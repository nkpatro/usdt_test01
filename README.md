# USDT Balance API

A production-ready FastAPI service for querying USDT balances across multiple blockchain networks including Ethereum (ERC-20), Tron (TRC-20), and Solana (SPL).

## Features

- ✅ **Multi-chain support**: Query USDT balances across Ethereum, Tron, and Solana
- ✅ **Concurrent queries**: Fetch balances from all chains simultaneously
- ✅ **Address validation**: Automatic validation for each supported chain format
- ✅ **Robust error handling**: Graceful handling of individual chain failures
- ✅ **Dependency injection**: Clean architecture with injected blockchain clients
- ✅ **Retry logic**: Automatic retry with exponential backoff for RPC failures
- ✅ **Comprehensive logging**: Detailed logging for monitoring and debugging
- ✅ **Production-ready**: Docker support, health checks, and monitoring endpoints
- ✅ **OpenAPI documentation**: Interactive API documentation with Swagger UI

## Supported Networks

| Chain | Token Type | Contract/Mint Address | Decimals |
|-------|------------|-----------------------|----------|
| Ethereum | ERC-20 | `0xdAC17F958D2ee523a2206206994597C13D831ec7` | 6 |
| Tron | TRC-20 | `TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t` | 6 |
| Solana | SPL | `Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB` | 6 |

## Installation

### Prerequisites

- Python 3.11+
- Required API keys for blockchain RPC endpoints

### Setup

1. **Clone the repository:**
```bash
git clone <repository-url>
cd usdt-balance-api
```

2. **Install dependencies:**
```bash
pip install -r requirements.txt
```

3. **Configure environment:**
```bash
cp .env.example .env
# Edit .env with your API keys and configuration
```

4. **Start the server:**
```bash
python run.py
```

## Configuration

### Environment Variables

Create a `.env` file based on `.env.example`:

```env
# Required: Blockchain RPC endpoints
ETHEREUM_RPC_ENDPOINT=https://mainnet.infura.io/v3/YOUR_API_KEY
SOLANA_RPC_ENDPOINT=https://api.mainnet-beta.solana.com

# Optional: Server configuration
HOST=0.0.0.0
PORT=8000
LOG_LEVEL=INFO
RPC_TIMEOUT=30
```

### API Keys

You'll need API keys for:
- **Ethereum**: [Infura](https://infura.io/) or [Alchemy](https://alchemy.com/)
- **Tron**: Uses public endpoints (no key required)
- **Solana**: Uses public endpoints (no key required)

## API Endpoints

### Main Endpoint

#### `GET /balances/{address}`

Get USDT balances for a given address across all supported chains.

**Parameters:**
- `address` (path): Cryptocurrency address to query

**Response:**
```json
{
  "address": "0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b",
  "balances": [
    {"chain": "ethereum", "balance": "1000.000000"},
    {"chain": "tron", "balance": "500.000000"},
    {"chain": "solana", "balance": "250.000000"}
  ]
}
```

**Status Codes:**
- `200`: Success
- `400`: Invalid address format
- `502`: All blockchain services unavailable
- `500`: Internal server error

### Utility Endpoints

#### `GET /balances/health`
Check blockchain client connection status.

#### `GET /balances/supported-chains`
Get information about supported blockchain networks.

#### `GET /health`
Basic API health check.

#### `GET /`
API information and available endpoints.

## Usage Examples

### Using cURL

```bash
# Get USDT balance for Ethereum address
curl "http://localhost:8000/balances/0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b"

# Get USDT balance for Tron address
curl "http://localhost:8000/balances/TRX9rKKSGdyWS11jGPGJPw7G2HVpwfcNTL"

# Get USDT balance for Solana address
curl "http://localhost:8000/balances/11111111111111111111111111111112"

# Check API health
curl "http://localhost:8000/health"
```

### Using Python

```python
import httpx
import asyncio

async def get_usdt_balance(address: str):
    async with httpx.AsyncClient() as client:
        response = await client.get(f"http://localhost:8000/balances/{address}")
        return response.json()

# Example usage
balance = asyncio.run(get_usdt_balance("0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b"))
print(balance)
```

### Using JavaScript

```javascript
async function getUsdtBalance(address) {
    const response = await fetch(`http://localhost:8000/balances/${address}`);
    return await response.json();
}

// Example usage
getUsdtBalance("0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b")
    .then(balance => console.log(balance));
```

## Running the API

### Development Mode

```bash
# Start with auto-reload
python run.py --reload

# Start with debug logging
python run.py --log-level debug

# Start on different port
python run.py --port 8080
```

### Production Mode

```bash
# Start with production settings
python run.py --host 0.0.0.0 --port 8000

# Or use uvicorn directly
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

### Docker

```bash
# Build image
docker build -t usdt-balance-api .

# Run container
docker run -p 8000:8000 --env-file .env usdt-balance-api
```

## Testing

### Run Test Suite

```bash
# Start the API server first
python run.py

# In another terminal, run tests
python test_api.py
```

### Manual Testing

1. **Visit the interactive documentation:**
   - Swagger UI: http://localhost:8000/docs
   - ReDoc: http://localhost:8000/redoc

2. **Test with sample addresses:**
   - Ethereum: `0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b`
   - Tron: `TRX9rKKSGdyWS11jGPGJPw7G2HVpwfcNTL`
   - Solana: `11111111111111111111111111111112`

## Error Handling

The API provides comprehensive error handling:

- **400 Bad Request**: Invalid address format or empty address
- **502 Bad Gateway**: All blockchain RPC services are unavailable
- **500 Internal Server Error**: Unexpected server errors
- **503 Service Unavailable**: Blockchain clients not initialized

Individual chain failures are handled gracefully:
- If one chain fails, results from other chains are still returned
- Failed chains are logged but don't affect the overall response
- Only when ALL chains fail does the API return 502

## Monitoring

### Health Checks

```bash
# Basic API health
curl http://localhost:8000/health

# Blockchain client health
curl http://localhost:8000/balances/health

# Service metrics
curl http://localhost:8000/metrics
```

### Logging

Logs are written to both console and file (`app.log`):
- Request/response logging
- RPC error logging
- Performance metrics
- Client connection status

## Architecture

### Project Structure

```
usdt-balance-api/
├── app/
│   ├── __init__.py          # Package initialization
│   ├── main.py              # FastAPI application
│   ├── models.py            # Pydantic models
│   ├── clients.py           # Blockchain clients
│   ├── dependencies.py      # Dependency injection
│   └── router.py            # API routes
├── requirements.txt         # Dependencies
├── .env.example            # Environment template
├── Dockerfile              # Container configuration
├── run.py                  # Startup script
├── test_api.py             # Test suite
└── README.md               # This file
```

### Key Components

1. **Models** (`models.py`): Pydantic models for request/response validation
2. **Clients** (`clients.py`): Blockchain client implementations with retry logic
3. **Dependencies** (`dependencies.py`): Dependency injection configuration
4. **Router** (`router.py`): API endpoint implementations
5. **Main** (`main.py`): FastAPI application setup and middleware

## Performance

### Concurrent Requests

The API handles concurrent requests efficiently:
- Blockchain queries are executed in parallel
- Connection pooling for RPC clients
- Automatic retry with exponential backoff
- Request timeouts and circuit breakers

### Caching

For production deployment, consider adding:
- Redis caching for balance results
- Rate limiting per client
- Connection pooling optimization

## Security

### Production Considerations

1. **Environment Variables**: Never commit API keys to version control
2. **HTTPS**: Always use HTTPS in production
3. **Rate Limiting**: Implement rate limiting for public APIs
4. **Input Validation**: All inputs are validated and sanitized
5. **Error Handling**: Errors don't expose sensitive information

### Network Security

- Supports trusted host middleware
- CORS configuration for cross-origin requests
- Request timeout limits
- Input sanitization and validation

## Deployment

### Production Deployment

1. **Environment Setup:**
```bash
# Production environment file
cp .env.example .env.production
# Configure with production values
```

2. **Docker Deployment:**
```bash
docker build -t usdt-balance-api .
docker run -p 8000:8000 --env-file .env.production usdt-balance-api
```

3. **Kubernetes Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: usdt-balance-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: usdt-balance-api
  template:
    metadata:
      labels:
        app: usdt-balance-api
    spec:
      containers:
      - name: api
        image: usdt-balance-api:latest
        ports:
        - containerPort: 8000
        env:
        - name: ETHEREUM_RPC_ENDPOINT
          valueFrom:
            secretKeyRef:
              name: api-secrets
              key: ethereum-rpc-endpoint
```

### Load Balancing

For high availability:
- Use multiple API instances behind a load balancer
- Implement health checks for automatic failover
- Configure appropriate timeouts and retries

## Troubleshooting

### Common Issues

1. **RPC Connection Errors:**
   - Check API keys in `.env` file
   - Verify network connectivity
   - Check rate limits on RPC providers

2. **Invalid Address Errors:**
   - Ensure address format matches the chain
   - Check for typos in address strings

3. **Timeout Errors:**
   - Increase `RPC_TIMEOUT` in configuration
   - Check RPC provider status

### Debug Mode

```bash
# Enable debug logging
python run.py --log-level debug

# Check client connections
curl http://localhost:8000/balances/health
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License. See LICENSE file for details.

## Support

For issues and questions:
- Check the API documentation at `/docs`
- Review the logs for error details
- Open an issue on GitHub
- Check RPC provider documentation

---

**Built with ❤️ by Senior Backend Engineer**