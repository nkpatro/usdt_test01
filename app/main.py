import logging
import os
from contextlib import asynccontextmanager
from typing import AsyncGenerator

from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.trustedhost import TrustedHostMiddleware

from .router import router as balances_router
from .dependencies import get_client_manager


# Configure logging
def setup_logging():
    """Configure application logging."""
    log_level = os.getenv("LOG_LEVEL", "INFO").upper()
    log_format = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
    
    logging.basicConfig(
        level=getattr(logging, log_level),
        format=log_format,
        handlers=[
            logging.StreamHandler(),
            logging.FileHandler("app.log") if os.getenv("LOG_FILE", "").strip() else None
        ]
    )
    
    # Set specific logger levels
    logging.getLogger("web3").setLevel(logging.WARNING)
    logging.getLogger("tronpy").setLevel(logging.WARNING)
    logging.getLogger("solana").setLevel(logging.WARNING)
    logging.getLogger("httpx").setLevel(logging.WARNING)
    logging.getLogger("uvicorn").setLevel(logging.INFO)


# Setup logging
setup_logging()
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    """
    Application lifespan context manager.
    
    Handles startup and shutdown events for the FastAPI application.
    """
    # Startup
    logger.info("Starting USDT Balance API service")
    
    try:
        # Initialize blockchain client manager
        manager = get_client_manager()
        logger.info("Blockchain clients initialized successfully")
        
        # Store manager in app state for cleanup
        app.state.client_manager = manager
        
        yield
        
    except Exception as e:
        logger.error(f"Failed to initialize application: {e}")
        raise
    finally:
        # Shutdown
        logger.info("Shutting down USDT Balance API service")
        
        # Cleanup blockchain clients
        if hasattr(app.state, 'client_manager'):
            try:
                await app.state.client_manager.close()
                logger.info("Blockchain clients closed successfully")
            except Exception as e:
                logger.error(f"Error closing blockchain clients: {e}")


# Create FastAPI application
app = FastAPI(
    title="USDT Balance API",
    description="""
    Multi-chain USDT balance query API supporting Ethereum (ERC-20), Tron (TRC-20), and Solana (SPL) networks.
    
    ## Features
    - **Multi-chain support**: Query USDT balances across Ethereum, Tron, and Solana
    - **Concurrent queries**: Fetch balances from all chains simultaneously
    - **Robust error handling**: Graceful handling of individual chain failures
    - **Address validation**: Automatic validation for each supported chain format
    - **Production-ready**: Comprehensive logging, monitoring, and health checks
    
    ## Supported Networks
    - **Ethereum**: ERC-20 USDT (0xdAC17F958D2ee523a2206206994597C13D831ec7)
    - **Tron**: TRC-20 USDT (TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t)
    - **Solana**: SPL USDT (Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB)
    """,
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=os.getenv("CORS_ORIGINS", "*").split(","),
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)

# Add trusted host middleware for production
if os.getenv("TRUSTED_HOSTS"):
    app.add_middleware(
        TrustedHostMiddleware,
        allowed_hosts=os.getenv("TRUSTED_HOSTS", "").split(",")
    )

# Include routers
app.include_router(balances_router)


# Global exception handler
@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException) -> JSONResponse:
    """Handle HTTP exceptions with proper logging."""
    logger.warning(f"HTTP {exc.status_code} error for {request.url}: {exc.detail}")
    return JSONResponse(
        status_code=exc.status_code,
        content={"detail": exc.detail}
    )


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    """Handle unexpected exceptions."""
    logger.error(f"Unexpected error for {request.url}: {exc}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"}
    )


# Root endpoint
@app.get(
    "/",
    summary="API Information",
    description="Get basic information about the USDT Balance API service"
)
async def root() -> dict:
    """Root endpoint with API information."""
    return {
        "service": "USDT Balance API",
        "version": "1.0.0",
        "description": "Multi-chain USDT balance query service",
        "supported_chains": ["ethereum", "tron", "solana"],
        "endpoints": {
            "balance_query": "/balances/{address}",
            "health_check": "/balances/health",
            "supported_chains": "/balances/supported-chains",
            "documentation": "/docs"
        }
    }


# Health check endpoint
@app.get(
    "/health",
    summary="Service Health Check",
    description="Basic health check for the API service"
)
async def health() -> dict:
    """Basic health check endpoint."""
    return {"status": "healthy", "service": "USDT Balance API"}


# Metrics endpoint for monitoring
@app.get(
    "/metrics",
    summary="Service Metrics",
    description="Basic metrics for monitoring and observability"
)
async def metrics() -> dict:
    """Basic metrics endpoint."""
    return {
        "service": "USDT Balance API",
        "version": "1.0.0",
        "uptime": "Available via health check",
        "supported_chains": 3,
        "endpoints": len(app.routes)
    }


if __name__ == "__main__":
    import uvicorn
    
    # Configuration from environment variables
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", "8000"))
    log_level = os.getenv("LOG_LEVEL", "info").lower()
    
    # Run the application
    uvicorn.run(
        "app.main:app",
        host=host,
        port=port,
        log_level=log_level,
        reload=os.getenv("RELOAD", "false").lower() == "true"
    )