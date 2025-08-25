#!/bin/bash

echo "🧪 TESTING CORS CONFIGURATION"
echo "================================"

# Test CORS từ localhost:3000 (Admin/Seller Frontend)
echo "📱 Testing CORS from localhost:3000 (Admin/Seller Frontend)"
curl -s -X OPTIONS http://localhost:8080/api/v1/users/profile \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v 2>&1 | grep -E "(Access-Control-Allow-Origin|HTTP/1.1)"

echo -e "\n"

# Test CORS từ localhost:3001 (User Frontend)
echo "📱 Testing CORS from localhost:3001 (User Frontend)"
curl -s -X OPTIONS http://localhost:8080/api/v1/users/profile \
  -H "Origin: http://localhost:3001" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v 2>&1 | grep -E "(Access-Control-Allow-Origin|HTTP/1.1)"

echo -e "\n"

# Test CORS từ Vercel Admin/Seller Frontend
echo "🌐 Testing CORS from Vercel Admin/Seller Frontend"
curl -s -X OPTIONS http://localhost:8080/api/v1/users/profile \
  -H "Origin: https://simplecommerce-mdm-as-frontend.vercel.app" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v 2>&1 | grep -E "(Access-Control-Allow-Origin|HTTP/1.1)"

echo -e "\n"

# Test CORS từ Vercel User Frontend
echo "🌐 Testing CORS from Vercel User Frontend"
curl -s -X OPTIONS http://localhost:8080/api/v1/users/profile \
  -H "Origin: https://simplecommerce-user-frontend.vercel.app" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v 2>&1 | grep -E "(Access-Control-Allow-Origin|HTTP/1.1)"

echo -e "\n"

# Test actual API call với CORS headers
echo "🔍 Testing actual API call with CORS headers"
curl -s -X GET http://localhost:8080/api/v1/users/profile \
  -H "Origin: https://simplecommerce-mdm-as-frontend.vercel.app" \
  -H "Authorization: Bearer test-token" \
  -v 2>&1 | grep -E "(Access-Control-Allow-Origin|HTTP/1.1|Unauthorized)"

echo -e "\n✅ CORS testing completed!"
