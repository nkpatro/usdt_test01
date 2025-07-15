import asyncio
import logging
from decimal import Decimal
from typing import Optional, Dict, Any
from contextlib import asynccontextmanager

from web3 import Web3
from web3.exceptions import Web3Exception
from tronpy import Tron
from tronpy.exceptions import TronError
from solana.rpc.async_api import AsyncClient as SolanaClient
from solana.rpc.types import TokenAccountOpts
from solders.pubkey import Pubkey
from solders.rpc.responses import GetTokenAccountsByOwnerResp
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type

from .models import ChainConfig, AddressValidator


logger = logging.getLogger(__name__)


class EthereumClient:
    """Ethereum blockchain client for USDT balance queries."""
    
    def __init__(self, rpc_endpoint: str, timeout: int = 30):
        """Initialize Ethereum client with Web3 connection."""
        self.rpc_endpoint = rpc_endpoint
        self.timeout = timeout
        self.web3 = Web3(Web3.HTTPProvider(rpc_endpoint, request_kwargs={'timeout': timeout}))
        
        # ERC-20 ABI for balanceOf function
        self.erc20_abi = [
            {
                "constant": True,
                "inputs": [{"name": "_owner", "type": "address"}],
                "name": "balanceOf",
                "outputs": [{"name": "balance", "type": "uint256"}],
                "type": "function"
            }
        ]
        
        # USDT contract
        self.usdt_contract = self.web3.eth.contract(
            address=Web3.to_checksum_address(ChainConfig.USDT_CONTRACTS["ethereum"]),
            abi=self.erc20_abi
        )
    
    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=4, max=10),
        retry=retry_if_exception_type((Web3Exception, Exception))
    )
    async def get_usdt_balance(self, address: str) -> Decimal:
        """
        Get USDT balance for Ethereum address.
        
        Args:
            address: Ethereum address to query
            
        Returns:
            USDT balance as Decimal
            
        Raises:
            Web3Exception: If RPC call fails
            ValueError: If address is invalid
        """
        try:
            # Validate address format
            if not AddressValidator.is_valid_ethereum_address(address):
                raise ValueError(f"Invalid Ethereum address format: {address}")
            
            # Convert to checksum address
            checksum_address = Web3.to_checksum_address(address)
            
            # Call balanceOf function
            balance_wei = await asyncio.to_thread(
                self.usdt_contract.functions.balanceOf(checksum_address).call
            )
            
            # Convert from wei to USDT (6 decimals)
            balance = Decimal(balance_wei) / (10 ** ChainConfig.USDT_DECIMALS["ethereum"])
            
            logger.info(f"Ethereum USDT balance for {address}: {balance}")
            return balance
            
        except Web3Exception as e:
            logger.error(f"Ethereum RPC error for address {address}: {e}")
            raise
        except Exception as e:
            logger.error(f"Unexpected error getting Ethereum balance for {address}: {e}")
            raise
    
    def is_connected(self) -> bool:
        """Check if client is connected to Ethereum network."""
        try:
            return self.web3.is_connected()
        except Exception:
            return False


class TronClient:
    """Tron blockchain client for USDT balance queries."""
    
    def __init__(self, network: str = "mainnet", timeout: int = 30):
        """Initialize Tron client."""
        self.network = network
        self.timeout = timeout
        self.tron = Tron(network=network, timeout=timeout)
        self.usdt_contract_address = ChainConfig.USDT_CONTRACTS["tron"]
    
    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=4, max=10),
        retry=retry_if_exception_type((TronError, Exception))
    )
    async def get_usdt_balance(self, address: str) -> Decimal:
        """
        Get USDT balance for Tron address.
        
        Args:
            address: Tron address to query
            
        Returns:
            USDT balance as Decimal
            
        Raises:
            TronError: If RPC call fails
            ValueError: If address is invalid
        """
        try:
            # Validate address format
            if not AddressValidator.is_valid_tron_address(address):
                raise ValueError(f"Invalid Tron address format: {address}")
            
            # Get TRC-20 token balance
            balance_response = await asyncio.to_thread(
                self.tron.get_account_balance,
                address,
                self.usdt_contract_address
            )
            
            # Extract balance from response
            balance_raw = balance_response if isinstance(balance_response, (int, float)) else 0
            
            # Convert from smallest unit to USDT (6 decimals)
            balance = Decimal(balance_raw) / (10 ** ChainConfig.USDT_DECIMALS["tron"])
            
            logger.info(f"Tron USDT balance for {address}: {balance}")
            return balance
            
        except TronError as e:
            logger.error(f"Tron RPC error for address {address}: {e}")
            raise
        except Exception as e:
            logger.error(f"Unexpected error getting Tron balance for {address}: {e}")
            raise
    
    def is_connected(self) -> bool:
        """Check if client is connected to Tron network."""
        try:
            # Try to get latest block to test connection
            self.tron.get_latest_block()
            return True
        except Exception:
            return False


class SolanaClient:
    """Solana blockchain client for USDT balance queries."""
    
    def __init__(self, rpc_endpoint: str, timeout: int = 30):
        """Initialize Solana client."""
        self.rpc_endpoint = rpc_endpoint
        self.timeout = timeout
        self.client = SolanaClient(rpc_endpoint, timeout=timeout)
        self.usdt_mint = Pubkey.from_string(ChainConfig.USDT_CONTRACTS["solana"])
    
    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=4, max=10),
        retry=retry_if_exception_type((Exception,))
    )
    async def get_usdt_balance(self, address: str) -> Decimal:
        """
        Get USDT balance for Solana address.
        
        Args:
            address: Solana address to query
            
        Returns:
            USDT balance as Decimal
            
        Raises:
            Exception: If RPC call fails
            ValueError: If address is invalid
        """
        try:
            # Validate address format
            if not AddressValidator.is_valid_solana_address(address):
                raise ValueError(f"Invalid Solana address format: {address}")
            
            # Convert address to Pubkey
            owner_pubkey = Pubkey.from_string(address)
            
            # Get token accounts for USDT
            token_accounts_response = await self.client.get_token_accounts_by_owner(
                owner_pubkey,
                TokenAccountOpts(mint=self.usdt_mint),
                commitment="confirmed"
            )
            
            total_balance = Decimal(0)
            
            # Sum balances from all USDT token accounts
            if token_accounts_response.value:
                for account in token_accounts_response.value:
                    if account.account.data.parsed["info"]["tokenAmount"]["uiAmount"]:
                        balance_amount = Decimal(
                            str(account.account.data.parsed["info"]["tokenAmount"]["uiAmount"])
                        )
                        total_balance += balance_amount
            
            logger.info(f"Solana USDT balance for {address}: {total_balance}")
            return total_balance
            
        except Exception as e:
            logger.error(f"Solana RPC error for address {address}: {e}")
            raise
    
    async def is_connected(self) -> bool:
        """Check if client is connected to Solana network."""
        try:
            await self.client.get_health()
            return True
        except Exception:
            return False
    
    async def close(self):
        """Close the async client connection."""
        await self.client.close()


class BlockchainClientManager:
    """Manager for all blockchain clients with dependency injection."""
    
    def __init__(
        self,
        eth_rpc_endpoint: str,
        tron_network: str = "mainnet",
        sol_rpc_endpoint: str,
        timeout: int = 30
    ):
        """Initialize all blockchain clients."""
        self.eth_client = EthereumClient(eth_rpc_endpoint, timeout)
        self.tron_client = TronClient(tron_network, timeout)
        self.sol_client = SolanaClient(sol_rpc_endpoint, timeout)
        
        logger.info("Blockchain clients initialized")
    
    async def get_all_usdt_balances(self, address: str) -> Dict[str, Optional[Decimal]]:
        """
        Get USDT balances from all supported chains.
        
        Args:
            address: Address to query (will be validated per chain)
            
        Returns:
            Dictionary mapping chain names to balances (None if error)
        """
        balances = {}
        
        # Create tasks for concurrent balance fetching
        tasks = []
        
        # Ethereum
        if AddressValidator.is_valid_ethereum_address(address):
            tasks.append(("ethereum", self.eth_client.get_usdt_balance(address)))
        
        # Tron
        if AddressValidator.is_valid_tron_address(address):
            tasks.append(("tron", self.tron_client.get_usdt_balance(address)))
        
        # Solana
        if AddressValidator.is_valid_solana_address(address):
            tasks.append(("solana", self.sol_client.get_usdt_balance(address)))
        
        # Execute all tasks concurrently
        if tasks:
            results = await asyncio.gather(
                *[task[1] for task in tasks],
                return_exceptions=True
            )
            
            # Process results
            for i, (chain, _) in enumerate(tasks):
                result = results[i]
                if isinstance(result, Exception):
                    logger.error(f"Failed to get {chain} balance: {result}")
                    balances[chain] = None
                else:
                    balances[chain] = result
        
        return balances
    
    def get_connection_status(self) -> Dict[str, bool]:
        """Get connection status for all clients."""
        return {
            "ethereum": self.eth_client.is_connected(),
            "tron": self.tron_client.is_connected(),
            "solana": asyncio.create_task(self.sol_client.is_connected())
        }
    
    async def close(self):
        """Close all client connections."""
        await self.sol_client.close()
        logger.info("All blockchain clients closed")