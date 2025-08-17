# 🧪 Manual Testing Guide: Add and Delete Variant

## **📋 PREREQUISITES:**
- Application đang chạy trên `http://localhost:8080`
- Product ID 1 đã tồn tại với 1 variant
- Bạn cần có JWT token của seller

---

## **🔑 STEP 1: Get Authentication Token**

### **Option A: Use Existing User (if available)**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "existing_user@example.com",
    "password": "existing_password"
  }'
```

### **Option B: Create New User (requires admin access)**
```bash
curl -X POST "http://localhost:8080/api/v1/admin/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {ADMIN_TOKEN}" \
  -d '{
    "email": "testseller@example.com",
    "password": "test123",
    "firstName": "Test",
    "lastName": "Seller",
    "phone": "0123456789",
    "role": "SELLER"
  }'
```

---

## **📦 STEP 2: Add New Variant to Product**

### **Current State:**
- Product ID: 1
- Current Variants: 1 (TEST-SKU-001)

### **Add Variant Request:**
```bash
curl -X PUT "http://localhost:8080/api/v1/seller/products/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {YOUR_JWT_TOKEN}" \
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
```

### **Expected Response:**
```json
{
  "statusCode": 200,
  "message": "Product updated successfully",
  "data": {
    "id": 1,
    "name": "Sản phẩm Test từ Postman",
    "variants": [
      {
        "id": 1,
        "sku": "TEST-SKU-001",
        "finalPrice": 1500000.00
      },
      {
        "id": 2,
        "sku": "TEST-SKU-002",
        "finalPrice": 1600000.00
      }
    ]
  }
}
```

---

## **✅ STEP 3: Verify Variant Added**

```bash
curl "http://localhost:8080/api/v1/products/latest"
```

**Expected:** Product có 2 variants

---

## **🗑️ STEP 4: Delete New Variant**

### **Delete Request:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/seller/products/1/variants/2" \
  -H "Authorization: Bearer {YOUR_JWT_TOKEN}"
```

### **Expected Response:**
```json
{
  "statusCode": 200,
  "message": "Variant deleted successfully",
  "data": null
}
```

---

## **✅ STEP 5: Verify Variant Deleted**

```bash
curl "http://localhost:8080/api/v1/products/latest"
```

**Expected:** Product có 1 variant (back to original state)

---

## **🔧 USING POSTMAN:**

### **1. Import Collection:**
- File: `Product_Variant_Management.postman_collection.json`

### **2. Set Variables:**
- `base_url`: `http://localhost:8080`
- `seller_token`: `{YOUR_JWT_TOKEN}`
- `product_id`: `1`

### **3. Test Flow:**
1. **Login** → Get JWT token
2. **Update Product** → Add variant
3. **Get Products** → Verify variant added
4. **Delete Variant** → Remove variant
5. **Get Products** → Verify variant deleted

---

## **🚨 TROUBLESHOOTING:**

### **If "Phone is required":**
- Đảm bảo phone field được include trong request

### **If "Bad credentials":**
- Kiểm tra email/password
- User có thể chưa tồn tại

### **If "403 Forbidden":**
- JWT token không hợp lệ hoặc hết hạn
- User không có quyền SELLER

### **If "Cannot delete the only variant":**
- Product chỉ có 1 variant
- Cần ít nhất 2 variants để test deletion

---

## **🎯 SUCCESS CRITERIA:**

✅ **Variant Addition:**
- Product có 2 variants sau khi update
- New variant có ID mới và SKU "TEST-SKU-002"

✅ **Variant Deletion:**
- Product có 1 variant sau khi delete
- Variant count giảm từ 2 xuống 1
- Product trở về trạng thái ban đầu

✅ **API Responses:**
- Status code: 200
- Proper success messages
- Data structure consistent

---

## **📝 NOTES:**

- **Authentication required** cho tất cả seller operations
- **Variant ID** sẽ được tự động generate khi tạo mới
- **Soft delete** được sử dụng (variant bị ẩn, không bị xóa hoàn toàn)
- **Cascade delete** đảm bảo relationship consistency

**Happy Testing! 🚀**
