import logging
from typing import List, Optional
from decimal import Decimal

from fastapi import APIRouter, HTTPException, status, Path
from fastapi.responses import JSONResponse

from .models import BalanceResponse, ChainBalance, ChainConfig, AddressValidator
from .dependencies import ManagerDep, AddressDep
from .clients import BlockchainClientManager


logger = logging.getLogger(__name__)

# Create router instance
router = APIRouter(
    prefix="/balances",
    tags=["balances"],
    responses={
        400: {"description": "Bad Request - Invalid address format"},
        502: {"description": "Bad Gateway - All blockchain RPC services unavailable"},
        503: {"description": "Service Unavailable - Blockchain clients not initialized"}
    }
)


@router.get(
    "/{address}",
    response_model=BalanceResponse,
    summary="Get USDT balances across all supported chains",
    description="""
    Get USDT balance for a given address across Ethereum (ERC-20), Tron (TRC-20), and Solana (SPL) networks.
    
    - **address**: Cryptocurrency address to query. The address format will be validated for each supported chain.
    - Returns balances for all chains where the address format is valid.
    - If all chains fail to respond, returns HTTP 502 (Bad Gateway).
    - Individual chain failures are logged but don't affect the response for other chains.
    """,
    responses={
        200: {
            "description": "Successfully retrieved balances",
            "content": {
                "application/json": {
                    "example": {
                        "address": "0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b",
                        "balances": [
                            {"chain": "ethereum", "balance": "1000.000000"},
                            {"chain": "tron", "balance": "500.000000"},
                            {"chain": "solana", "balance": "250.000000"}
                        ]
                    }
                }
            }
        },
        400: {
            "description": "Invalid address format",
            "content": {
                "application/json": {
                    "example": {"detail": "Address must be a non-empty string"}
                }
            }
        },
        502: {
            "description": "All blockchain services unavailable",
            "content": {
                "application/json": {
                    "example": {"detail": "All blockchain RPC services are currently unavailable"}
                }
            }
        }
    }
)
async def get_usdt_balances(
    address: str = Path(..., description="Cryptocurrency address to query"),
    manager: ManagerDep = None
) -> BalanceResponse:
    """
    Get USDT balances for a given address across all supported blockchain networks.
    
    This endpoint queries USDT balances from:
    - Ethereum (ERC-20): Contract 0xdAC17F958D2ee523a2206206994597C13D831ec7
    - Tron (TRC-20): Contract TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t
    - Solana (SPL): Mint Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB
    
    Args:
        address: Cryptocurrency address to query
        manager: Injected blockchain client manager
        
    Returns:
        BalanceResponse with balances for all valid chains
        
    Raises:
        HTTPException: 400 for invalid address, 502 if all chains fail
    """
    # Validate address is non-empty string (done by dependency)
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
    
    logger.info(f"Fetching USDT balances for address: {address}")
    
    # Validate address format for at least one chain
    valid_chains = []
    for chain in ChainConfig.SUPPORTED_CHAINS:
        if AddressValidator.validate_address_for_chain(address, chain):
            valid_chains.append(chain)
    
    if not valid_chains:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Address format is not valid for any supported chain: {ChainConfig.SUPPORTED_CHAINS}"
        )
    
    logger.info(f"Address {address} is valid for chains: {valid_chains}")
    
    try:
        # Get balances from all chains concurrently
        chain_balances = await manager.get_all_usdt_balances(address)
        
        # Build response balances list
        response_balances = []
        successful_chains = []
        
        for chain in ChainConfig.SUPPORTED_CHAINS:
            balance = chain_balances.get(chain)
            
            if balance is not None:
                # Successfully got balance for this chain
                chain_balance = ChainBalance(
                    chain=chain,
                    balance=balance
                )
                response_balances.append(chain_balance)
                successful_chains.append(chain)
                logger.info(f"Successfully fetched {chain} balance: {balance}")
            else:
                # Failed to get balance for this chain
                logger.warning(f"Failed to fetch balance for {chain}")
        
        # Check if we got at least one successful response
        if not successful_chains:
            # All chains failed
            logger.error(f"All blockchain RPC services failed for address: {address}")
            raise HTTPException(
                status_code=status.HTTP_502_BAD_GATEWAY,
                detail="All blockchain RPC services are currently unavailable"
            )
        
        # Log successful and failed chains
        if len(successful_chains) != len(valid_chains):
            failed_chains = [chain for chain in valid_chains if chain not in successful_chains]
            logger.warning(f"Partial success - failed chains: {failed_chains}, successful chains: {successful_chains}")
        
        # Create response
        response = BalanceResponse(
            address=address,
            balances=response_balances
        )
        
        logger.info(f"Successfully fetched balances for address {address} from {len(successful_chains)} chains")
        return response
        
    except HTTPException:
        # Re-raise HTTP exceptions (like 502)
        raise
    except Exception as e:
        # Log unexpected errors
        logger.error(f"Unexpected error fetching balances for address {address}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Internal server error while fetching balances"
        )


@router.get(
    "/health",
    summary="Health check for blockchain clients",
    description="Check the connection status of all blockchain clients",
    responses={
        200: {"description": "Service health status"},
        503: {"description": "Service unhealthy - some or all clients disconnected"}
    }
)
async def health_check(manager: ManagerDep) -> JSONResponse:
    """
    Health check endpoint for blockchain client connections.
    
    Returns:
        JSONResponse with connection status for all clients
    """
    try:
        # Get connection status for all clients
        status_dict = manager.get_connection_status()
        
        # Check if all clients are connected
        all_connected = all(status_dict.values())
        
        if all_connected:
            return JSONResponse(
                status_code=status.HTTP_200_OK,
                content={
                    "status": "healthy",
                    "clients": status_dict,
                    "message": "All blockchain clients are connected"
                }
            )
        else:
            return JSONResponse(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                content={
                    "status": "unhealthy",
                    "clients": status_dict,
                    "message": "Some blockchain clients are disconnected"
                }
            )
            
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        return JSONResponse(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            content={
                "status": "error",
                "message": f"Health check failed: {str(e)}"
            }
        )


@router.get(
    "/supported-chains",
    summary="Get supported blockchain networks",
    description="Returns information about all supported blockchain networks and their USDT contract addresses"
)
async def get_supported_chains() -> JSONResponse:
    """
    Get information about supported blockchain networks.
    
    Returns:
        JSONResponse with supported chains and their configurations
    """
    return JSONResponse(
        content={
            "supported_chains": ChainConfig.SUPPORTED_CHAINS,
            "usdt_contracts": ChainConfig.USDT_CONTRACTS,
            "usdt_decimals": ChainConfig.USDT_DECIMALS,
            "description": "USDT balance query service supports these blockchain networks"
        }
    )