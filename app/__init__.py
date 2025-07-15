"""
USDT Balance API

A production-ready FastAPI service for querying USDT balances across multiple blockchain networks.

Supported Networks:
- Ethereum (ERC-20)
- Tron (TRC-20)
- Solana (SPL)

Features:
- Multi-chain concurrent balance queries
- Robust error handling and retry logic
- Comprehensive logging and monitoring
- Address validation for all supported chains
- Dependency injection for blockchain clients
- Production-ready configuration
"""

__version__ = "1.0.0"
__author__ = "Senior Backend Engineer"
__email__ = "backend@example.com"

from .main import app

__all__ = ["app"]