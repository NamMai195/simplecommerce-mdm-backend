#!/bin/bash

echo "🧪 TESTING CORS FOR VERCEL FRONTENDS"
echo "====================================="

# Test 1: OPTIONS request từ Vercel Admin/Seller Frontend
echo "🌐 Test 1: OPTIONS request from Vercel Admin/Seller Frontend"
echo "URL: https://sc-mdm-api.nammai.id.vn/api/v1/admin/users/statistics"
echo "Origin: https://simplecommerce-mdm-as-frontend.vercel.app"
echo "---"

curl -s -X OPTIONS "https://sc-mdm-api.nammai.id.vn/api/v1/admin/users/statistics" \
  -H "Origin: https://simplecommerce-mdm-as-frontend.vercel.app" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v 2>&1 | grep -E "(HTTP|Access-Control|403|200|404|Forbidden)"

echo -e "\n"

# Test 2: GET request từ Vercel Admin/Seller Frontend
echo "🌐 Test 2: GET request from Vercel Admin/Seller Frontend"
echo "URL: https://sc-mdm-api.nammai.id.vn/api/v1/admin/users/statistics"
echo "Origin: https://simplecommerce-mdm-as-frontend.vercel.app"
echo "---"

curl -s -X GET "https://sc-mdm-api.nammai.id.vn/api/v1/admin/users/statistics" \
  -H "Origin: https://simplecommerce-mdm-as-frontend.vercel.app" \
  -v 2>&1 | grep -E "(HTTP|Access-Control|403|200|404|Forbidden|Unauthorized)"

echo -e "\n"

# Test 3: Public endpoint để so sánh
echo "🌐 Test 3: Public endpoint for comparison"
echo "URL: https://sc-mdm-api.nammai.id.vn/api/v1/products"
echo "Origin: https://simplecommerce-mdm-as-frontend.vercel.app"
echo "---"

curl -s -X GET "https://sc-mdm-api.nammai.id.vn/api/v1/products" \
  -H "Origin: https://simplecommerce-mdm-as-frontend.vercel.app" \
  -v 2>&1 | grep -E "(HTTP|Access-Control|403|200|404|Forbidden|products|data)"

echo -e "\n"

# Test 4: Health check endpoint
echo "🌐 Test 4: Health check endpoint"
echo "URL: https://sc-mdm-api.nammai.id.vn/actuator/health"
echo "---"

curl -s -X GET "https://sc-mdm-api.nammai.id.vn/actuator/health" \
  -v 2>&1 | grep -E "(HTTP|status|UP|DOWN|403|200|404)"

echo -e "\n✅ CORS testing for Vercel frontends completed!"
echo "💡 Expected results:"
echo "   - OPTIONS requests should return 200 with CORS headers"
echo "   - GET requests should return 200/401 (not 403 CORS error)"
echo "   - Access-Control-Allow-Origin should be present"
