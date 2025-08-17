#!/bin/bash

# Quick Test Commands for Variant Addition and Deletion
echo "🚀 Quick Test Commands for Variant Addition and Deletion"
echo "========================================================"

BASE_URL="http://localhost:8080"

echo -e "\n${YELLOW}📋 CURRENT STATE:${NC}"
echo "Product ID: 1"
echo "Current Variants: 1 (TEST-SKU-001)"

echo -e "\n${GREEN}🔑 STEP 1: Get JWT Token (Replace with your credentials)${NC}"
cat << 'EOF'
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "your_email@example.com",
    "password": "your_password"
  }'
EOF

echo -e "\n${GREEN}📦 STEP 2: Add New Variant${NC}"
cat << EOF
curl -X PUT "http://localhost:8080/api/v1/seller/products/1" \\
  -H "Content-Type: application/json" \\
  -H "Authorization: Bearer {YOUR_JWT_TOKEN}" \\
  -d '{
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
        "options": "{\\"color\\": \\"Đỏ\\", \\"size\\": \\"M\\"}"
      },
      {
        "id": null,
        "sku": "TEST-SKU-002",
        "finalPrice": 1600000.00,
        "stockQuantity": 30,
        "options": "{\\"color\\": \\"Xanh\\", \\"size\\": \\"L\\"}"
      }
    ]
  }'
EOF

echo -e "\n${GREEN}✅ STEP 3: Verify Variant Added${NC}"
echo "curl \"$BASE_URL/api/v1/products/latest\""

echo -e "\n${GREEN}🗑️ STEP 4: Delete New Variant (Replace {NEW_VARIANT_ID} with actual ID)${NC}"
cat << EOF
curl -X DELETE "http://localhost:8080/api/v1/seller/products/1/variants/{NEW_VARIANT_ID}" \\
  -H "Authorization: Bearer {YOUR_JWT_TOKEN}"
EOF

echo -e "\n${GREEN}✅ STEP 5: Verify Variant Deleted${NC}"
echo "curl \"$BASE_URL/api/v1/products/latest\""

echo -e "\n${YELLOW}📝 INSTRUCTIONS:${NC}"
echo "1. Copy và paste từng command vào terminal"
echo "2. Replace {YOUR_JWT_TOKEN} với token từ step 1"
echo "3. Replace {NEW_VARIANT_ID} với ID của variant mới (thường là 2)"
echo "4. Chạy từng step theo thứ tự"

echo -e "\n${GREEN}🎯 EXPECTED RESULTS:${NC}"
echo "• Step 2: Product có 2 variants"
echo "• Step 4: Variant bị xóa thành công"
echo "• Step 5: Product có 1 variant (back to original)"

echo -e "\n✅ Ready for testing!"
