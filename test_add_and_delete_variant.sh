#!/bin/bash

# Test Add and Delete Variant
echo "🧪 Test Add and Delete Variant for Product 1"
echo "============================================="

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Check current product state
echo -e "\n${YELLOW}1. Checking Current Product State...${NC}"
PRODUCT_RESPONSE=$(curl -s "$BASE_URL/api/v1/products/latest")
PRODUCT_ID=$(echo "$PRODUCT_RESPONSE" | jq -r '.data.products[0].id // empty')
VARIANT_COUNT=$(echo "$PRODUCT_RESPONSE" | jq -r '.data.products[0].variants | length // 0')

echo "Product ID: $PRODUCT_ID"
echo "Current Variant Count: $VARIANT_COUNT"

if [ "$VARIANT_COUNT" -eq 0 ]; then
    echo -e "${RED}❌ No variants found. Cannot test.${NC}"
    exit 1
fi

# Test 2: Show current variants
echo -e "\n${YELLOW}2. Current Variants:${NC}"
echo "$PRODUCT_RESPONSE" | jq -r '.data.products[0].variants[] | "SKU: \(.sku), Price: \(.finalPrice), Stock: \(.stockQuantity)"'

# Test 3: Try to add variant using update product (this would require auth)
echo -e "\n${YELLOW}3. Note: Adding variant requires authentication${NC}"
echo "To add variant, you need to:"
echo "1. Login as seller"
echo "2. Use PUT /api/v1/seller/products/{id} with new variants in request body"
echo "3. Include new variant without ID (ID = null means create new)"

# Test 4: Show the update product API structure
echo -e "\n${YELLOW}4. Update Product API Structure for Adding Variant:${NC}"
cat << 'EOF'
PUT /api/v1/seller/products/{id}
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "name": "Sản phẩm Test từ Postman",
  "description": "Mô tả chi tiết cho sản phẩm.",
  "basePrice": 1500000.00,
  "categoryId": 1,
  "variants": [
    {
      "id": 1,
      "sku": "TEST-SKU-001",
      "finalPrice": 1500000.00,
      "stockQuantity": 50,
      "options": "{\"color\": \"Đỏ\", \"size\": \"M\"}"
    },
    {
      "id": null,
      "sku": "TEST-SKU-002",
      "finalPrice": 1600000.00,
      "stockQuantity": 30,
      "options": "{\"color\": \"Xanh\", \"size\": \"L\"}"
    }
  ]
}
EOF

# Test 5: Show delete variant API
echo -e "\n${YELLOW}5. Delete Variant API:${NC}"
echo "DELETE $BASE_URL/api/v1/seller/products/$PRODUCT_ID/variants/{variantId}"
echo "Headers: Authorization: Bearer {JWT_TOKEN}"

# Test 6: Manual test instructions
echo -e "\n${YELLOW}6. Manual Testing Steps:${NC}"
echo "1. Login as seller: POST $BASE_URL/api/v1/auth/login"
echo "2. Add variant: PUT $BASE_URL/api/v1/seller/products/$PRODUCT_ID"
echo "3. Verify variant added: GET $BASE_URL/api/v1/products/latest"
echo "4. Delete variant: DELETE $BASE_URL/api/v1/seller/products/$PRODUCT_ID/variants/{newVariantId}"
echo "5. Verify variant deleted: GET $BASE_URL/api/v1/products/latest"

echo -e "\n${GREEN}✅ Test script completed!${NC}"
echo "Use the manual testing steps above to test variant addition and deletion."
