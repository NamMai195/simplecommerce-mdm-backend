# 🧪 ORDER MANAGEMENT API TESTING GUIDE

## **📋 OVERVIEW**
Hướng dẫn test toàn bộ Order Management APIs (COD) đã được implement.

## **🎯 FEATURES ĐÃ IMPLEMENT:**
- ✅ **Checkout Process** - Tạo order từ cart với COD payment
- ✅ **User Order Management** - View history, details, cancel
- ✅ **Seller Order Management** - Confirm, ship, deliver orders
- ✅ **Admin Order Management** - Overview, search, statistics
- ✅ **Multi-shop Support** - Orders split by shop automatically
- ✅ **COD Payment** - Cash on Delivery workflow
- ✅ **Status Tracking** - Complete order lifecycle

---

## **🚀 SETUP TESTING ENVIRONMENT**

### **1. Start Application:**
```bash
# Start database
docker-compose up -d db

# Start application 
./mvnw spring-boot:run

# Application runs on: http://localhost:8080
```

### **2. Import Postman Collections:**
- **Collection**: `Order_Management_Test.postman_collection.json`
- **Environment**: `SimpleCommerce_Order_Test.postman_environment.json`

### **3. Sample Test Users:**
```
USER (Buyer):
- Email: user@simplecommerce.com
- Password: user123

SELLER:
- Email: seller@simplecommerce.com  
- Password: seller123

ADMIN:
- Email: admin@simplecommerce.com
- Password: admin123
```

---

## **🧪 TESTING FLOW**

### **Phase 1: Authentication**
1. **User Login** - Get buyer access token
2. **Seller Login** - Get seller access token  
3. **Admin Login** - Get admin access token

### **Phase 2: Setup Test Data**
1. **Get Categories** - Retrieve sample categories
2. **Create Product** (Seller) - Create test product
3. **Approve Product** (Admin) - Make product available

### **Phase 3: Cart Operations**
1. **Clear Cart** - Start fresh
2. **Add Items** - Add product variants to cart
3. **View Cart** - Verify cart contents

### **Phase 4: Order Creation (COD)**
1. **Checkout** - Convert cart to order
   ```json
   POST /api/v1/orders
   {
     "shippingAddress": "123 Nguyễn Văn A, P.1, Q.1, TP.HCM",
     "customerPhone": "+84901234567",
     "notesToSeller": "Giao hàng buổi chiều",
     "paymentMethodCode": "COD"
   }
   ```

2. **Expected Result:**
   - Order created with `AWAITING_CONFIRMATION` status
   - MasterOrder + individual Orders per shop
   - COD Payment record (PENDING)
   - Cart cleared automatically

### **Phase 5: User Order Management**
1. **Get Order History** - View user's orders
2. **Get Order Details** - Detailed order info
3. **Get Master Order** - Full order group details
4. **Cancel Order** - If still in early status

### **Phase 6: Seller Order Management**
1. **View Orders** - Seller's shop orders
2. **Get Pending Orders** - AWAITING_CONFIRMATION status
3. **Confirm Order** - AWAITING_CONFIRMATION → PROCESSING
4. **Ship Order** - PROCESSING → SHIPPED
5. **Mark Delivered** - SHIPPED → DELIVERED

### **Phase 7: Admin Order Management**
1. **View All Orders** - System-wide overview
2. **Search Orders** - By customer email, order number
3. **Get Statistics** - Dashboard metrics
4. **Admin Override** - Force status changes

---

## **📋 API ENDPOINTS**

### **🛍️ User/Buyer APIs:**
```
POST   /api/v1/orders                    - Checkout (Create order)
GET    /api/v1/orders                    - Order history
GET    /api/v1/orders/{id}               - Order details
GET    /api/v1/orders/master/{id}        - Master order details
PUT    /api/v1/orders/{id}/cancel        - Cancel order
```

### **🏪 Seller APIs:**
```
GET    /api/v1/seller/orders             - Seller orders
PUT    /api/v1/seller/orders/{id}/status - Update status
```

### **👑 Admin APIs:**
```
GET    /api/v1/admin/orders              - All orders
GET    /api/v1/admin/orders/search       - Search orders  
GET    /api/v1/admin/orders/statistics   - Statistics
PUT    /api/v1/admin/orders/{id}/status  - Admin override
```

---

## **🔄 ORDER STATUS FLOW**

### **COD Order Lifecycle:**
```
1. AWAITING_CONFIRMATION (Initial COD status)
   ↓ (Seller confirms)
2. PROCESSING (Preparing items)
   ↓ (Seller ships)
3. SHIPPED (In transit)
   ↓ (Seller marks delivered)
4. DELIVERED (Customer received)
   ↓ (Payment collected)
5. COMPLETED (Order finished)
```

### **Status Transition Rules:**
- **User can cancel**: AWAITING_CONFIRMATION only
- **Seller can confirm**: AWAITING_CONFIRMATION → PROCESSING
- **Seller can ship**: PROCESSING → SHIPPED  
- **Seller can deliver**: SHIPPED → DELIVERED
- **System completes**: DELIVERED → COMPLETED (after COD payment)

---

## **💳 COD PAYMENT WORKFLOW**

### **Payment Creation:**
- **Status**: PENDING (until delivery)
- **Transaction ID**: "COD-{orderGroupNumber}"
- **Amount**: Total order amount
- **Method**: Cash on Delivery

### **Payment Completion:**
- When order status = DELIVERED
- Payment status = COMPLETED
- Order can transition to COMPLETED

---

## **🧪 EXPECTED TEST RESULTS**

### **✅ Successful Checkout:**
```json
{
  "statusCode": 201,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "orderGroupNumber": "MO1703...",
    "overallStatus": "AWAITING_CONFIRMATION",
    "totalAmountPaid": 60000000,
    "paymentMethodSnapshot": "Cash on Delivery",
    "orders": [
      {
        "id": 1,
        "orderNumber": "ORD1703...",
        "shopName": "John's Electronics Store",
        "orderStatus": "AWAITING_CONFIRMATION",
        "totalAmount": 60000000,
        "orderItems": [...]
      }
    ]
  }
}
```

### **✅ Order History:**
```json
{
  "statusCode": 200,
  "data": {
    "content": [
      {
        "orderNumber": "ORD1703...",
        "shopName": "John's Electronics Store", 
        "orderStatus": "PROCESSING",
        "totalAmount": 60000000,
        "totalItems": 2,
        "totalQuantity": 2,
        "customerEmail": "user@simplecommerce.com"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### **✅ Seller Order Management:**
```json
{
  "statusCode": 200,
  "data": {
    "orderNumber": "ORD1703...",
    "orderStatus": "PROCESSING",
    "shopName": "John's Electronics Store",
    "customerEmail": "user@simplecommerce.com",
    "totalAmount": 60000000,
    "notesToSeller": "Giao hàng buổi chiều"
  }
}
```

### **✅ Admin Statistics:**
```json
{
  "statusCode": 200,
  "data": {
    "totalOrders": 5,
    "awaitingConfirmationOrders": 1,
    "processingOrders": 2,
    "shippedOrders": 1,
    "deliveredOrders": 1,
    "completedOrders": 0,
    "cancelledOrders": 0
  }
}
```

---

## **🚨 ERROR SCENARIOS TO TEST**

### **❌ Checkout Errors:**
1. **Empty Cart** - Should return 400
2. **Invalid Payment Method** - Should return 400
3. **Insufficient Stock** - Should return 400
4. **Missing Address** - Should return 400

### **❌ Authorization Errors:**
1. **No Token** - Should return 401
2. **Wrong User** - Should return 404/403
3. **Wrong Role** - Should return 403

### **❌ Status Transition Errors:**
1. **Invalid Transition** - SHIPPED → PROCESSING (not allowed)
2. **Cancel Processed Order** - Should return 400
3. **User Cancel Others' Order** - Should return 404

---

## **📊 SUCCESS METRICS**

### **✅ All Tests Pass When:**
1. **Authentication works** for all user types
2. **Checkout creates orders** successfully
3. **Multi-shop splitting** works correctly
4. **Status transitions** follow business rules
5. **COD payment** records created properly
6. **Cart is cleared** after checkout
7. **Authorization** enforced correctly
8. **Search and filtering** work as expected
9. **Statistics** calculate correctly
10. **Error handling** appropriate

---

## **🎯 SPECIFIC TEST CASES**

### **Test Case 1: Complete COD Flow**
```
1. User adds 2 iPhone variants to cart
2. User checkouts with COD
3. Order created with AWAITING_CONFIRMATION
4. Seller confirms → PROCESSING
5. Seller ships → SHIPPED  
6. Seller delivers → DELIVERED
7. Admin marks complete → COMPLETED
8. Verify payment status updated
```

### **Test Case 2: Multi-Shop Order**
```
1. Add products from different shops to cart
2. Checkout should create:
   - 1 MasterOrder
   - Multiple Orders (one per shop)
   - Correct totals per shop
```

### **Test Case 3: Authorization**
```
1. User can only see own orders
2. Seller can only see own shop orders
3. Admin can see all orders
4. User cannot access seller/admin APIs
```

### **Test Case 4: Error Handling**
```
1. Try checkout with empty cart
2. Try cancel already shipped order
3. Try access other user's order
4. Try invalid status transitions
```

---

## **💡 TIPS FOR TESTING**

### **🔧 Postman Tips:**
1. **Run in sequence** - Authentication → Setup → Orders
2. **Check console logs** - Environment variables set correctly
3. **Verify responses** - Status codes and data structure
4. **Use variables** - orderIds, tokens auto-extracted

### **🐛 Debugging:**
1. **Check logs** - Application console for errors
2. **Database state** - Verify data persisted correctly
3. **Token validity** - Re-login if 401 errors
4. **Product approval** - Ensure products approved before ordering

### **📈 Performance Testing:**
1. **Multiple concurrent orders** - Test under load
2. **Large cart sizes** - Many items checkout
3. **Pagination** - Large order lists
4. **Search performance** - With many orders

---

## **🎉 SUCCESS CONFIRMATION**

**Order Management API implementation is COMPLETE when:**

✅ **All Postman tests pass**  
✅ **Full order flow works end-to-end**  
✅ **Multi-shop orders handled correctly**  
✅ **COD payment workflow functional**  
✅ **All user roles work as expected**  
✅ **Error handling robust**  
✅ **Performance acceptable**  

**🚀 READY FOR PRODUCTION!** 