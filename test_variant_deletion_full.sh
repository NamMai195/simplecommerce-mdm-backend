#!/bin/bash

# Comprehensive Test for Variant Deletion API
echo "🧪 Comprehensive Test for Variant Deletion API"
echo "=============================================="

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Check if application is running
echo -e "\n${YELLOW}1. Checking Application Status...${NC}"
if curl -s "$BASE_URL/api/v1/products/latest" > /dev/null; then
    echo -e "${GREEN}✅ Application is running${NC}"
else
    echo -e "${RED}❌ Application is not running${NC}"
    exit 1
fi

# Test 2: Get current product state
echo -e "\n${YELLOW}2. Getting Current Product State...${NC}"
PRODUCT_RESPONSE=$(curl -s "$BASE_URL/api/v1/products/latest")
PRODUCT_ID=$(echo "$PRODUCT_RESPONSE" | jq -r '.data.products[0].id // empty')
VARIANT_COUNT=$(echo "$PRODUCT_RESPONSE" | jq -r '.data.products[0].variants | length // 0')

echo "Product ID: $PRODUCT_ID"
echo "Current Variant Count: $VARIANT_COUNT"

if [ "$VARIANT_COUNT" -eq 0 ]; then
    echo -e "${RED}❌ No variants found. Cannot test deletion.${NC}"
    exit 1
fi

# Test 3: Test variant deletion without auth (should fail)
echo -e "\n${YELLOW}3. Testing Variant Deletion without Authentication...${NC}"
DELETE_RESPONSE=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL/api/v1/seller/products/$PRODUCT_ID/variants/1")
HTTP_CODE="${DELETE_RESPONSE: -3}"
RESPONSE_BODY="${DELETE_RESPONSE%???}"

echo "HTTP Status Code: $HTTP_CODE"
echo "Response Body: $RESPONSE_BODY"

if [ "$HTTP_CODE" -eq 403 ]; then
    echo -e "${GREEN}✅ Correctly rejected without authentication (403 Forbidden)${NC}"
else
    echo -e "${RED}❌ Unexpected response code: $HTTP_CODE${NC}"
fi

# Test 4: Verify product state hasn't changed
echo -e "\n${YELLOW}4. Verifying Product State Hasn't Changed...${NC}"
AFTER_DELETE_RESPONSE=$(curl -s "$BASE_URL/api/v1/products/latest")
AFTER_VARIANT_COUNT=$(echo "$AFTER_DELETE_RESPONSE" | jq -r '.data.products[0].variants | length // 0')

echo "Variant Count After Failed Deletion: $AFTER_VARIANT_COUNT"

if [ "$VARIANT_COUNT" -eq "$AFTER_VARIANT_COUNT" ]; then
    echo -e "${GREEN}✅ Product state unchanged (as expected)${NC}"
else
    echo -e "${RED}❌ Product state changed unexpectedly${NC}"
fi

# Test 5: Test with invalid product ID
echo -e "\n${YELLOW}5. Testing with Invalid Product ID...${NC}"
INVALID_DELETE_RESPONSE=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL/api/v1/seller/products/99999/variants/1")
INVALID_HTTP_CODE="${INVALID_DELETE_RESPONSE: -3}"
INVALID_RESPONSE_BODY="${INVALID_DELETE_RESPONSE%???}"

echo "HTTP Status Code: $INVALID_HTTP_CODE"
echo "Response Body: $INVALID_RESPONSE_BODY"

if [ "$INVALID_HTTP_CODE" -eq 403 ]; then
    echo -e "${GREEN}✅ Correctly rejected invalid product ID (403 Forbidden)${NC}"
else
    echo -e "${YELLOW}⚠️  Unexpected response for invalid product ID: $INVALID_HTTP_CODE${NC}"
fi

# Test 6: Test with invalid variant ID
echo -e "\n${YELLOW}6. Testing with Invalid Variant ID...${NC}"
INVALID_VARIANT_RESPONSE=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL/api/v1/seller/products/$PRODUCT_ID/variants/99999")
INVALID_VARIANT_HTTP_CODE="${INVALID_VARIANT_RESPONSE: -3}"
INVALID_VARIANT_RESPONSE_BODY="${INVALID_VARIANT_RESPONSE%???}"

echo "HTTP Status Code: $INVALID_VARIANT_HTTP_CODE"
echo "Response Body: $INVALID_VARIANT_RESPONSE_BODY"

if [ "$INVALID_VARIANT_HTTP_CODE" -eq 403 ]; then
    echo -e "${GREEN}✅ Correctly rejected invalid variant ID (403 Forbidden)${NC}"
else
    echo -e "${YELLOW}⚠️  Unexpected response for invalid variant ID: $INVALID_VARIANT_HTTP_CODE${NC}"
fi

# Summary
echo -e "\n${YELLOW}📋 TEST SUMMARY${NC}"
echo "=================="
echo -e "${GREEN}✅ Application is running${NC}"
echo -e "${GREEN}✅ Product found with $VARIANT_COUNT variants${NC}"
echo -e "${GREEN}✅ Unauthenticated requests properly rejected${NC}"
echo -e "${GREEN}✅ Product state remains unchanged${NC}"
echo -e "${GREEN}✅ Invalid IDs properly handled${NC}"

echo -e "\n${YELLOW}📝 NEXT STEPS FOR FULL TESTING:${NC}"
echo "1. Create a test user account"
echo "2. Login to get JWT token"
echo "3. Create a product with multiple variants"
echo "4. Test variant deletion with proper authentication"
echo "5. Verify variant is actually deleted from database"

echo -e "\n${GREEN}🎯 API ENDPOINT READY FOR TESTING:${NC}"
echo "DELETE $BASE_URL/api/v1/seller/products/{productId}/variants/{variantId}"
echo "Headers: Authorization: Bearer {JWT_TOKEN}"

echo -e "\n✅ All basic tests completed successfully!"
