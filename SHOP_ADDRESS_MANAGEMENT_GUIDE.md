# Shop Address Management API Testing Guide

## Overview
This guide covers testing the Shop Address Management APIs for sellers in SimpleCommerce MDM backend.

## Prerequisites
1. **Authentication**: You need a valid JWT token from login with SELLER role
2. **Shop Account**: An approved shop account
3. **User Addresses**: At least one address created for the user
4. **Postman**: For API testing

## Authentication Setup
1. **Login to get JWT token:**
   ```
   POST /api/v1/auth/login
   {
     "email": "seller@example.com",
     "password": "password123"
   }
   ```
2. **Copy the accessToken from response**
3. **Add Authorization header to all requests:**
   ```
   Authorization: Bearer <your_jwt_token>
   ```

## API Endpoints

### 1. Update Shop Address
**Endpoint:** `PUT /api/v1/seller/shop/address`

**Request Body:**
```json
{
  "addressId": 1
}
```

**Expected Response:**
```json
{
  "statusCode": 200,
  "message": "Shop address updated successfully",
  "data": {
    "id": 1,
    "name": "My Shop",
    "slug": "my-shop",
    "description": "A great shop",
    "contactEmail": "seller@example.com",
    "contactPhone": "0123456789",
    "addressLine1": "123 Nguyen Van Linh",
    "addressLine2": "Floor 5, Room 501",
    "city": "Ho Chi Minh",
    "postalCode": "700000",
    "country": "VN",
    "totalProducts": 5,
    "isActive": true,
    "rating": 4.5
  }
}
```

### 2. Get Seller Shop (to verify address update)
**Endpoint:** `GET /api/v1/seller/shop`

**Expected Response:** Same as above, showing updated address

## Test Scenarios

### Scenario 1: Update Shop Address
1. Create a user address first using Address Management APIs
2. Get the address ID from the response
3. Use that address ID to update shop address
4. Verify shop details show the new address

### Scenario 2: Address Validation
1. Try to update shop address with non-existent address ID
2. Verify proper error message
3. Try to update shop address without SELLER role
4. Verify access denied

### Scenario 3: Shop Address in Shop Creation
1. Create shop with address ID during creation
2. Verify shop is created with the specified address
3. Update shop address later
4. Verify address is updated

## Integration with Address Management

### Workflow:
1. **User creates addresses** using `/api/v1/addresses` (Address Management)
2. **User creates shop** using `/api/v1/shops` (can include address ID)
3. **Seller updates shop address** using `/api/v1/seller/shop/address`
4. **Address snapshots** are stored in orders when customers checkout

### Benefits:
- **Single Address per Shop**: Each shop has exactly one address (simple management)
- **Address Reuse**: Users can use their existing addresses for their shop
- **Consistent Data**: Shop address uses the same Address entity as user addresses
- **Easy Updates**: Sellers can easily change shop address using address ID

## Error Handling

### Common Error Responses

**400 Bad Request:**
```json
{
  "statusCode": 400,
  "message": "Failed to update shop address: Address not found with id: 999"
}
```

**403 Forbidden:**
```json
{
  "statusCode": 403,
  "message": "Access denied: JWT expired"
}
```

**404 Not Found:**
```json
{
  "statusCode": 404,
  "message": "Seller has no shop"
}
```

## Notes
- Shop address is **optional** during shop creation
- Shop address can be **updated anytime** after shop approval
- Shop address **must exist** in the Address table
- Shop address is **displayed** in shop details and used for shipping calculations
- **One address per shop** - simple and easy to manage
