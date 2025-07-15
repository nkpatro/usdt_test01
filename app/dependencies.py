import os
import logging
from typing import Annotated
from functools import lru_cache

from fastapi import Depends, HTTPException, status
from .clients import BlockchainClientManager, EthereumClient, TronClient, SolanaClient


logger = logging.getLogger(__name__)


@lru_cache()
def get_blockchain_config() -> dict:
    """Get blockchain configuration from environment variables."""
    return {
        "eth_rpc_endpoint": os.getenv(
            "ETHEREUM_RPC_ENDPOINT", 
            "https://mainnet.infura.io/v3/YOUR_API_KEY"
        ),
        "tron_network": os.getenv("TRON_NETWORK", "mainnet"),
        "sol_rpc_endpoint": os.getenv(
            "SOLANA_RPC_ENDPOINT", 
            "https://api.mainnet-beta.solana.com"
        ),
        "timeout": int(os.getenv("RPC_TIMEOUT", "30"))
    }


@lru_cache()
def get_client_manager() -> BlockchainClientManager:
    """
    Create and return a singleton BlockchainClientManager instance.
    
    This is cached to ensure the same clients are reused across requests.
    """
    config = get_blockchain_config()
    
    try:
        manager = BlockchainClientManager(
            eth_rpc_endpoint=config["eth_rpc_endpoint"],
            tron_network=config["tron_network"],
            sol_rpc_endpoint=config["sol_rpc_endpoint"],
            timeout=config["timeout"]
        )
        
        logger.info("BlockchainClientManager created successfully")
        return manager
        
    except Exception as e:
        logger.error(f"Failed to create BlockchainClientManager: {e}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Failed to initialize blockchain clients"
        )


# Dependency injection functions
def get_eth_client(
    manager: Annotated[BlockchainClientManager, Depends(get_client_manager)]
) -> EthereumClient:
    """Get Ethereum client from manager."""
    return manager.eth_client


def get_tron_client(
    manager: Annotated[BlockchainClientManager, Depends(get_client_manager)]
) -> TronClient:
    """Get Tron client from manager."""
    return manager.tron_client


def get_sol_client(
    manager: Annotated[BlockchainClientManager, Depends(get_client_manager)]
) -> SolanaClient:
    """Get Solana client from manager."""
    return manager.sol_client


def get_manager(
    manager: Annotated[BlockchainClientManager, Depends(get_client_manager)]
) -> BlockchainClientManager:
    """Get the full blockchain client manager."""
    return manager


# Validation dependencies
def validate_address(address: str) -> str:
    """
    Validate that address is a non-empty string.
    
    Args:
        address: Address to validate
        
    Returns:
        Validated address string
        
    Raises:
        HTTPException: If address is invalid
    """
    if not address or not isinstance(address, str):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Address must be a non-empty string"
        )
    
    address = address.strip()
    if not address:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Address cannot be empty or whitespace only"
        )
    
    return address


# Type aliases for cleaner dependency injection
EthClientDep = Annotated[EthereumClient, Depends(get_eth_client)]
TronClientDep = Annotated[TronClient, Depends(get_tron_client)]
SolClientDep = Annotated[SolanaClient, Depends(get_sol_client)]
ManagerDep = Annotated[BlockchainClientManager, Depends(get_manager)]
AddressDep = Annotated[str, Depends(validate_address)]