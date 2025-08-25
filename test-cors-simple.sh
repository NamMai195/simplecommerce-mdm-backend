#!/bin/bash

echo "🧪 SIMPLE CORS TEST"
echo "=================="
echo ""

echo "🌐 Test 1: Simple OPTIONS request"
echo "URL: https://sc-mdm-api.nammai.id.vn/api/v1/admin/users/statistics"
echo "---"
curl -s -I -X OPTIONS \
     -H "Origin: https://simplecommerce-mdm-as-frontend.vercel.app" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: Authorization" \
     https://sc-mdm-api.nammai.id.vn/api/v1/admin/users/statistics
echo ""
echo ""

echo "🌐 Test 2: Simple GET request"
echo "URL: https://sc-mdm-api.nammai.id.vn/api/v1/admin/users/statistics"
echo "---"
curl -s -I -H "Origin: https://simplecommerce-mdm-as-frontend.vercel.app" \
     https://sc-mdm-api.nammai.id.vn/api/v1/admin/users/statistics
echo ""
echo ""

echo "🌐 Test 3: Public endpoint (should work)"
echo "URL: https://sc-mdm-api.nammai.id.vn/api/v1/products"
echo "---"
curl -s -I -H "Origin: https://simplecommerce-mdm-as-frontend.vercel.app" \
     https://sc-mdm-api.nammai.id.vn/api/v1/products
echo ""
echo ""

echo "✅ Simple CORS test completed!"
