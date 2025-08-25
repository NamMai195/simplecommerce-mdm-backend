#!/bin/bash

echo "🧪 TESTING CORS ISSUE - 403 Forbidden"
echo "======================================"

# Test 1: OPTIONS request (CORS preflight) - Endpoint bị lỗi
echo "🔍 Test 1: OPTIONS request to problematic endpoint"
echo "URL: https://sc-mdm-api.nammai.id.vn/api/v1/admin/users/statistics"
echo "Expected: Should return CORS headers, not 403"
echo "---"

curl -s -X OPTIONS "https://sc-mdm-api.nammai.id.vn/api/v1/admin/users/statistics" \
  -H "Origin: https://simplecommerce-mdm-as-frontend.vercel.app" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v 2>&1 | grep -E "(HTTP|Access-Control|403|200|404|Forbidden)"

echo -e "\n"

# Test 2: OPTIONS request - Public endpoint (không cần auth)
echo "🔍 Test 2: OPTIONS request to public endpoint"
echo "URL: https://sc-mdm-api.nammai.id.vn/api/v1/products"
echo "Expected: Should work without 403"
echo "---"

curl -s -X OPTIONS "https://sc-mdm-api.nammai.id.vn/api/v1/products" \
  -H "Origin: https://simplecommerce-mdm-as-frontend.vercel.app" \
  -H "Access-Control-Request-Method: GET" \
  -v 2>&1 | grep -E "(HTTP|Access-Control|403|200|404|Forbidden)"

echo -e "\n"

# Test 3: GET request - Public endpoint
echo "🔍 Test 3: GET request to public endpoint"
echo "URL: https://sc-mdm-api.nammai.id.vn/api/v1/products"
echo "Expected: Should return data, not 403"
echo "---"

curl -s -X GET "https://sc-mdm-api.nammai.id.vn/api/v1/products" \
  -H "Origin: https://simplecommerce-mdm-as-frontend.vercel.app" \
  -v 2>&1 | grep -E "(HTTP|Access-Control|403|200|404|Forbidden|products|data)"

echo -e "\n"

# Test 4: Health check endpoint
echo "🔍 Test 4: Health check endpoint"
echo "URL: https://sc-mdm-api.nammai.id.vn/actuator/health"
echo "Expected: Should return health status"
echo "---"

curl -s -X GET "https://sc-mdm-api.nammai.id.vn/actuator/health" \
  -v 2>&1 | grep -E "(HTTP|status|UP|DOWN|403|200|404)"

echo -e "\n"

# Test 5: Swagger docs endpoint
echo "🔍 Test 5: Swagger docs endpoint"
echo "URL: https://sc-mdm-api.nammai.id.vn/v3/api-docs"
echo "Expected: Should return API documentation"
echo "---"

curl -s -X GET "https://sc-mdm-api.nammai.id.vn/v3/api-docs" \
  -v 2>&1 | grep -E "(HTTP|swagger|openapi|403|200|404)"

echo -e "\n✅ CORS testing completed!"
echo "💡 If all OPTIONS requests return 403, the issue is likely:"
echo "   1. Cloudflare blocking OPTIONS requests"
echo "   2. Railway not configured for CORS preflight"
echo "   3. Spring Boot CORS config not applied"
