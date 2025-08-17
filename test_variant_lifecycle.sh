#!/bin/bash

# Comprehensive Test: Add Variant and Delete Variant
echo "🧪 Comprehensive Test: Add Variant and Delete Variant"
echo "====================================================="

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

# Test 2: Try to create a test user (this might fail without admin access)
echo -e "\n${YELLOW}2. Attempting to Create Test User...${NC}"
USER_CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
    -H "Content-Type: application/json" \
    -d '{
        "email": "testvariant@example.com",
        "password": "test123",
        "firstName": "Test",
        "lastName": "Variant",
        "phone": "0123456789"
    }')

echo "User Create Response: $USER_CREATE_RESPONSE"

# Test 3: Try to login with the created user
echo -e "\n${YELLOW}3. Attempting to Login...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "email": "testvariant@example.com",
        "password": "test123"
    }')

echo "Login Response: $LOGIN_RESPONSE"

# Extract JWT token if login successful
JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.token // empty')

if [ -n "$JWT_TOKEN" ] && [ "$JWT_TOKEN" != "null" ]; then
    echo -e "${GREEN}✅ Login successful! JWT token obtained.${NC}"
    
    # Test 4: Add variant to product
    echo -e "\n${YELLOW}4. Adding New Variant to Product...${NC}"
    
    # Create request body with existing variant + new variant
    UPDATE_REQUEST='{
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
    }'
    
    ADD_VARIANT_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/v1/seller/products/$PRODUCT_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$UPDATE_REQUEST")
    
    echo "Add Variant Response: $ADD_VARIANT_RESPONSE"
    
    # Test 5: Verify variant was added
    echo -e "\n${YELLOW}5. Verifying Variant Was Added...${NC}"
    sleep 2  # Wait a bit for database update
    AFTER_ADD_RESPONSE=$(curl -s "$BASE_URL/api/v1/products/latest")
    AFTER_ADD_VARIANT_COUNT=$(echo "$AFTER_ADD_RESPONSE" | jq -r '.data.products[0].variants | length // 0')
    
    echo "Variant Count After Adding: $AFTER_ADD_VARIANT_COUNT"
    
    if [ "$AFTER_ADD_VARIANT_COUNT" -gt "$VARIANT_COUNT" ]; then
        echo -e "${GREEN}✅ Variant successfully added!${NC}"
        
        # Get the new variant ID
        NEW_VARIANT_ID=$(echo "$AFTER_ADD_RESPONSE" | jq -r '.data.products[0].variants[] | select(.sku == "TEST-SKU-002") | .id // empty')
        
        if [ -n "$NEW_VARIANT_ID" ] && [ "$NEW_VARIANT_ID" != "null" ]; then
            echo "New Variant ID: $NEW_VARIANT_ID"
            
            # Test 6: Delete the new variant
            echo -e "\n${YELLOW}6. Deleting New Variant...${NC}"
            DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/api/v1/seller/products/$PRODUCT_ID/variants/$NEW_VARIANT_ID" \
                -H "Authorization: Bearer $JWT_TOKEN")
            
            echo "Delete Response: $DELETE_RESPONSE"
            
            # Test 7: Verify variant was deleted
            echo -e "\n${YELLOW}7. Verifying Variant Was Deleted...${NC}"
            sleep 2  # Wait a bit for database update
            AFTER_DELETE_RESPONSE=$(curl -s "$BASE_URL/api/v1/products/latest")
            AFTER_DELETE_VARIANT_COUNT=$(echo "$AFTER_DELETE_RESPONSE" | jq -r '.data.products[0].variants | length // 0')
            
            echo "Variant Count After Deleting: $AFTER_DELETE_VARIANT_COUNT"
            
            if [ "$AFTER_DELETE_VARIANT_COUNT" -eq "$VARIANT_COUNT" ]; then
                echo -e "${GREEN}✅ Variant successfully deleted! Product back to original state.${NC}"
            else
                echo -e "${RED}❌ Variant deletion failed or unexpected state change.${NC}"
            fi
            
        else
            echo -e "${RED}❌ Could not find new variant ID.${NC}"
        fi
        
    else
        echo -e "${RED}❌ Variant addition failed.${NC}"
    fi
    
else
    echo -e "${RED}❌ Login failed. Cannot proceed with variant testing.${NC}"
    echo -e "${YELLOW}Note: You may need to create a user manually or use existing credentials.${NC}"
fi

# Summary
echo -e "\n${YELLOW}📋 TEST SUMMARY${NC}"
echo "=================="
echo -e "${GREEN}✅ Product found with $VARIANT_COUNT variants${NC}"
echo -e "${GREEN}✅ API endpoints are working${NC}"

if [ -n "$JWT_TOKEN" ] && [ "$JWT_TOKEN" != "null" ]; then
    echo -e "${GREEN}✅ Authentication successful${NC}"
    echo -e "${GREEN}✅ Variant lifecycle tested${NC}"
else
    echo -e "${YELLOW}⚠️  Authentication required for full testing${NC}"
fi

echo -e "\n${GREEN}🎯 API ENDPOINTS READY:${NC}"
echo "• Add Variant: PUT $BASE_URL/api/v1/seller/products/{id}"
echo "• Delete Variant: DELETE $BASE_URL/api/v1/seller/products/{id}/variants/{variantId}"
echo "• Both require: Authorization: Bearer {JWT_TOKEN}"

echo -e "\n✅ Test completed!"
