from decimal import Decimal
from typing import List, Literal
from pydantic import BaseModel, Field, validator
import re


class ChainBalance(BaseModel):
    """Model representing USDT balance for a specific blockchain."""
    
    chain: Literal["ethereum", "tron", "solana"] = Field(
        ...,
        description="Blockchain network name"
    )
    balance: Decimal = Field(
        ...,
        description="USDT balance with proper decimal precision",
        ge=0
    )
    
    class Config:
        # Enable JSON serialization for Decimal
        json_encoders = {
            Decimal: str
        }
        schema_extra = {
            "example": {
                "chain": "ethereum",
                "balance": "1234.567890"
            }
        }


class BalanceResponse(BaseModel):
    """Response model for balance endpoint."""
    
    address: str = Field(..., description="The queried address")
    balances: List[ChainBalance] = Field(
        ...,
        description="USDT balances across all supported chains"
    )
    
    class Config:
        schema_extra = {
            "example": {
                "address": "0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b",
                "balances": [
                    {"chain": "ethereum", "balance": "1000.000000"},
                    {"chain": "tron", "balance": "500.000000"},
                    {"chain": "solana", "balance": "250.000000"}
                ]
            }
        }


class AddressValidator:
    """Utility class for address validation across different chains."""
    
    @staticmethod
    def is_valid_ethereum_address(address: str) -> bool:
        """Validate Ethereum address format."""
        return bool(re.match(r'^0x[a-fA-F0-9]{40}$', address))
    
    @staticmethod
    def is_valid_tron_address(address: str) -> bool:
        """Validate Tron address format."""
        return bool(re.match(r'^T[A-Za-z0-9]{33}$', address))
    
    @staticmethod
    def is_valid_solana_address(address: str) -> bool:
        """Validate Solana address format."""
        return bool(re.match(r'^[1-9A-HJ-NP-Za-km-z]{32,44}$', address))
    
    @staticmethod
    def validate_address_for_chain(address: str, chain: str) -> bool:
        """Validate address format for specific chain."""
        validators = {
            "ethereum": AddressValidator.is_valid_ethereum_address,
            "tron": AddressValidator.is_valid_tron_address,
            "solana": AddressValidator.is_valid_solana_address
        }
        return validators.get(chain, lambda x: False)(address)


class ChainConfig:
    """Configuration constants for different blockchain networks."""
    
    # USDT contract addresses
    USDT_CONTRACTS = {
        "ethereum": "0xdAC17F958D2ee523a2206206994597C13D831ec7",
        "tron": "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
        "solana": "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"
    }
    
    # Decimal places for USDT on each chain
    USDT_DECIMALS = {
        "ethereum": 6,
        "tron": 6,
        "solana": 6
    }
    
    # Chain identifiers
    SUPPORTED_CHAINS = ["ethereum", "tron", "solana"]
    
    # RPC endpoints (should be configured via environment variables)
    DEFAULT_RPC_ENDPOINTS = {
        "ethereum": "https://mainnet.infura.io/v3/YOUR_API_KEY",
        "tron": "https://api.trongrid.io",
        "solana": "https://api.mainnet-beta.solana.com"
    }