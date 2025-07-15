"""
Test script for USDT Balance API

This script demonstrates how to use the USDT Balance API and validates
the implementation with various test cases.
"""

import asyncio
import httpx
import json
from typing import Dict, List

# Test addresses for different chains
TEST_ADDRESSES = {
    "ethereum": {
        "valid": [
            "0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b",
            "0xdAC17F958D2ee523a2206206994597C13D831ec7",  # USDT contract
            "0x0000000000000000000000000000000000000000",  # Zero address
        ],
        "invalid": [
            "0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82",  # Too short
            "0xGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG",  # Invalid hex
            "742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b",   # Missing 0x
        ]
    },
    "tron": {
        "valid": [
            "TRX9rKKSGdyWS11jGPGJPw7G2HVpwfcNTL",
            "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",  # USDT contract
            "TLsV52sRDL79HXGGm9yzwKiJlokjXrlBV2",
        ],
        "invalid": [
            "TRX9rKKSGdyWS11jGPGJPw7G2HVpwfcNT",   # Too short
            "ARX9rKKSGdyWS11jGPGJPw7G2HVpwfcNTL",   # Wrong prefix
            "TRX9rKKSGdyWS11jGPGJPw7G2HVpwfcNTLL",  # Too long
        ]
    },
    "solana": {
        "valid": [
            "11111111111111111111111111111112",
            "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB",  # USDT mint
            "So11111111111111111111111111111111111111112",
        ],
        "invalid": [
            "11111111111111111111111111111111",  # Too short
            "111111111111111111111111111111111111111111111111",  # Too long
            "0000000000000000000000000000000000000000000",  # Invalid chars
        ]
    }
}

BASE_URL = "http://localhost:8000"


async def test_api_endpoints():
    """Test all API endpoints with various scenarios."""
    
    async with httpx.AsyncClient(timeout=30.0) as client:
        print("🚀 Testing USDT Balance API\n")
        
        # Test 1: Root endpoint
        print("1. Testing root endpoint...")
        response = await client.get(f"{BASE_URL}/")
        assert response.status_code == 200
        data = response.json()
        print(f"   ✅ Root endpoint: {data['service']}")
        
        # Test 2: Health check
        print("\n2. Testing health check...")
        response = await client.get(f"{BASE_URL}/health")
        assert response.status_code == 200
        data = response.json()
        print(f"   ✅ Health check: {data['status']}")
        
        # Test 3: Supported chains
        print("\n3. Testing supported chains endpoint...")
        response = await client.get(f"{BASE_URL}/balances/supported-chains")
        assert response.status_code == 200
        data = response.json()
        print(f"   ✅ Supported chains: {data['supported_chains']}")
        
        # Test 4: Blockchain health check
        print("\n4. Testing blockchain health check...")
        response = await client.get(f"{BASE_URL}/balances/health")
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"   ✅ Blockchain health: {data['status']}")
            print(f"   Clients: {data['clients']}")
        else:
            print(f"   ⚠️  Some blockchain clients may be unavailable")
        
        # Test 5: Valid addresses
        print("\n5. Testing valid addresses...")
        for chain, addresses in TEST_ADDRESSES.items():
            for address in addresses["valid"][:1]:  # Test first valid address
                print(f"   Testing {chain} address: {address}")
                response = await client.get(f"{BASE_URL}/balances/{address}")
                
                if response.status_code == 200:
                    data = response.json()
                    print(f"   ✅ Success: {len(data['balances'])} chains returned")
                    for balance in data['balances']:
                        print(f"      {balance['chain']}: {balance['balance']} USDT")
                elif response.status_code == 502:
                    print(f"   ⚠️  All chains unavailable (502)")
                else:
                    print(f"   ❌ Error {response.status_code}: {response.json()}")
        
        # Test 6: Invalid addresses
        print("\n6. Testing invalid addresses...")
        invalid_addresses = [
            "",  # Empty
            "   ",  # Whitespace
            "invalid_address",  # Invalid format
            "0x123",  # Too short
        ]
        
        for address in invalid_addresses:
            print(f"   Testing invalid address: '{address}'")
            response = await client.get(f"{BASE_URL}/balances/{address}")
            assert response.status_code == 400
            print(f"   ✅ Correctly rejected with 400")
        
        # Test 7: Cross-chain address (should work for compatible chains)
        print("\n7. Testing cross-chain compatibility...")
        eth_address = "0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b"
        response = await client.get(f"{BASE_URL}/balances/{eth_address}")
        
        if response.status_code == 200:
            data = response.json()
            print(f"   ✅ Ethereum address processed")
            print(f"   Valid for chains: {[b['chain'] for b in data['balances']]}")
        elif response.status_code == 502:
            print(f"   ⚠️  All chains unavailable")
        else:
            print(f"   Error: {response.status_code}")
        
        print("\n🎉 API testing completed!")


async def benchmark_api():
    """Benchmark API performance with concurrent requests."""
    
    print("\n📊 Benchmarking API Performance\n")
    
    async with httpx.AsyncClient(timeout=30.0) as client:
        # Test concurrent requests
        test_address = "0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b"
        
        print(f"Testing {test_address} with concurrent requests...")
        
        import time
        start_time = time.time()
        
        # Make 5 concurrent requests
        tasks = [
            client.get(f"{BASE_URL}/balances/{test_address}")
            for _ in range(5)
        ]
        
        responses = await asyncio.gather(*tasks, return_exceptions=True)
        
        end_time = time.time()
        duration = end_time - start_time
        
        print(f"   ⏱️  5 concurrent requests took {duration:.2f} seconds")
        
        # Analyze responses
        success_count = sum(1 for r in responses if hasattr(r, 'status_code') and r.status_code == 200)
        error_count = len(responses) - success_count
        
        print(f"   ✅ Successful requests: {success_count}")
        print(f"   ❌ Failed requests: {error_count}")
        
        if success_count > 0:
            avg_time = duration / success_count
            print(f"   📈 Average response time: {avg_time:.2f} seconds")


def print_usage_examples():
    """Print usage examples for the API."""
    
    print("\n📖 Usage Examples\n")
    
    print("1. Get USDT balance for Ethereum address:")
    print("   GET /balances/0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b")
    
    print("\n2. Get USDT balance for Tron address:")
    print("   GET /balances/TRX9rKKSGdyWS11jGPGJPw7G2HVpwfcNTL")
    
    print("\n3. Get USDT balance for Solana address:")
    print("   GET /balances/11111111111111111111111111111112")
    
    print("\n4. Check API health:")
    print("   GET /health")
    
    print("\n5. Check blockchain client health:")
    print("   GET /balances/health")
    
    print("\n6. Get supported chains:")
    print("   GET /balances/supported-chains")
    
    print("\nExample Response:")
    example_response = {
        "address": "0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b",
        "balances": [
            {"chain": "ethereum", "balance": "1000.000000"},
            {"chain": "tron", "balance": "500.000000"},
            {"chain": "solana", "balance": "250.000000"}
        ]
    }
    print(json.dumps(example_response, indent=2))


async def main():
    """Main test runner."""
    
    print("=" * 60)
    print("USDT Balance API - Test Suite")
    print("=" * 60)
    
    try:
        # Test API endpoints
        await test_api_endpoints()
        
        # Benchmark performance
        await benchmark_api()
        
        # Print usage examples
        print_usage_examples()
        
        print("\n" + "=" * 60)
        print("✅ All tests completed successfully!")
        print("=" * 60)
        
    except Exception as e:
        print(f"\n❌ Test failed: {e}")
        print("Make sure the API server is running on localhost:8000")
        print("Start with: python -m uvicorn app.main:app --host 0.0.0.0 --port 8000")


if __name__ == "__main__":
    asyncio.run(main())