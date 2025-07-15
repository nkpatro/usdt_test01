#!/usr/bin/env python3
"""
USDT Balance API Startup Script

This script provides an easy way to start the USDT Balance API with proper
configuration and environment setup.
"""

import os
import sys
import subprocess
import argparse
from pathlib import Path


def load_env_file(env_file: str = ".env"):
    """Load environment variables from file."""
    if not os.path.exists(env_file):
        print(f"Warning: {env_file} not found. Using default configuration.")
        return
    
    try:
        with open(env_file, 'r') as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#') and '=' in line:
                    key, value = line.split('=', 1)
                    os.environ[key] = value
        print(f"✅ Loaded environment from {env_file}")
    except Exception as e:
        print(f"Warning: Failed to load {env_file}: {e}")


def check_dependencies():
    """Check if required dependencies are installed."""
    required_packages = [
        'fastapi',
        'uvicorn',
        'web3',
        'tronpy',
        'solana',
        'pydantic',
        'httpx',
        'tenacity'
    ]
    
    missing_packages = []
    
    for package in required_packages:
        try:
            __import__(package)
        except ImportError:
            missing_packages.append(package)
    
    if missing_packages:
        print(f"❌ Missing required packages: {', '.join(missing_packages)}")
        print("Install with: pip install -r requirements.txt")
        return False
    
    print("✅ All dependencies are installed")
    return True


def validate_config():
    """Validate essential configuration."""
    required_env_vars = [
        'ETHEREUM_RPC_ENDPOINT',
        'SOLANA_RPC_ENDPOINT'
    ]
    
    warnings = []
    
    for var in required_env_vars:
        value = os.getenv(var)
        if not value or 'YOUR_API_KEY' in value:
            warnings.append(f"⚠️  {var} not configured properly")
    
    if warnings:
        print("\nConfiguration warnings:")
        for warning in warnings:
            print(f"  {warning}")
        print("\nThe API may not work properly without valid RPC endpoints.")
        print("Please configure your .env file with valid API keys.")
        return False
    
    print("✅ Configuration validated")
    return True


def start_server(
    host: str = "0.0.0.0",
    port: int = 8000,
    reload: bool = False,
    log_level: str = "info"
):
    """Start the FastAPI server."""
    
    print(f"\n🚀 Starting USDT Balance API server...")
    print(f"   Host: {host}")
    print(f"   Port: {port}")
    print(f"   Reload: {reload}")
    print(f"   Log Level: {log_level}")
    print(f"   Access: http://{host}:{port}")
    print(f"   Docs: http://{host}:{port}/docs")
    print(f"   ReDoc: http://{host}:{port}/redoc")
    print("\n" + "=" * 60)
    
    # Start uvicorn
    cmd = [
        sys.executable, "-m", "uvicorn",
        "app.main:app",
        "--host", host,
        "--port", str(port),
        "--log-level", log_level
    ]
    
    if reload:
        cmd.append("--reload")
    
    try:
        subprocess.run(cmd, check=True)
    except KeyboardInterrupt:
        print("\n\n👋 Server stopped by user")
    except subprocess.CalledProcessError as e:
        print(f"\n❌ Failed to start server: {e}")
        sys.exit(1)


def main():
    """Main function."""
    parser = argparse.ArgumentParser(
        description="Start the USDT Balance API server",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python run.py                          # Start with default settings
  python run.py --port 8080              # Start on port 8080
  python run.py --reload                 # Start with auto-reload for development
  python run.py --host 127.0.0.1         # Start on localhost only
  python run.py --log-level debug        # Enable debug logging
        """
    )
    
    parser.add_argument(
        "--host",
        default=os.getenv("HOST", "0.0.0.0"),
        help="Host to bind to (default: 0.0.0.0)"
    )
    
    parser.add_argument(
        "--port",
        type=int,
        default=int(os.getenv("PORT", 8000)),
        help="Port to bind to (default: 8000)"
    )
    
    parser.add_argument(
        "--reload",
        action="store_true",
        default=os.getenv("RELOAD", "false").lower() == "true",
        help="Enable auto-reload for development"
    )
    
    parser.add_argument(
        "--log-level",
        default=os.getenv("LOG_LEVEL", "info").lower(),
        choices=["debug", "info", "warning", "error", "critical"],
        help="Log level (default: info)"
    )
    
    parser.add_argument(
        "--env-file",
        default=".env",
        help="Environment file to load (default: .env)"
    )
    
    parser.add_argument(
        "--skip-checks",
        action="store_true",
        help="Skip dependency and configuration checks"
    )
    
    args = parser.parse_args()
    
    print("=" * 60)
    print("USDT Balance API - Startup Script")
    print("=" * 60)
    
    # Load environment file
    load_env_file(args.env_file)
    
    if not args.skip_checks:
        # Check dependencies
        if not check_dependencies():
            sys.exit(1)
        
        # Validate configuration
        if not validate_config():
            response = input("\nContinue anyway? (y/N): ")
            if response.lower() != 'y':
                sys.exit(1)
    
    # Start server
    start_server(
        host=args.host,
        port=args.port,
        reload=args.reload,
        log_level=args.log_level
    )


if __name__ == "__main__":
    main()