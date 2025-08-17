#!/bin/bash

# Test Variant Deletion API
echo "🧪 Testing Variant Deletion API"
echo "=================================="

BASE_URL="http://localhost:8080"

# Test 1: Health Check
echo "1. Testing Health Check..."
curl -s "$BASE_URL/api/v1/health" || echo "Health check failed (expected without auth)"

# Test 2: Get Latest Products
echo -e "\n2. Getting Latest Products..."
PRODUCT_RESPONSE=$(curl -s "$BASE_URL/api/v1/products/latest")
echo "Response: $PRODUCT_RESPONSE"

# Test 3: Test Variant Deletion without Auth (should fail)
echo -e "\n3. Testing Variant Deletion without Auth (should fail)..."
DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/api/v1/seller/products/1/variants/1")
echo "Delete Response: $DELETE_RESPONSE"

# Test 4: Check if product still has variants
echo -e "\n4. Checking if product still has variants..."
AFTER_DELETE_RESPONSE=$(curl -s "$BASE_URL/api/v1/products/latest")
echo "After Delete Response: $AFTER_DELETE_RESPONSE"

echo -e "\n✅ Test completed!"
echo "Note: Variant deletion requires authentication and at least 2 variants"
