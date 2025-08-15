# Address Management API Testing Guide

## Overview
This guide covers testing the Address Management APIs for the SimpleCommerce MDM backend.

## Prerequisites
1. **Authentication**: You need a valid JWT token from login
2. **User Account**: A registered user account with USER role
3. **Postman**: For API testing

## Authentication Setup
1. **Login to get JWT token:**
   ```
   POST /api/v1/auth/login
   {
     "email": "user@example.com",
     "password": "password123"
   }
   ```
2. **Copy the accessToken from response**
3. **Add Authorization header to all requests:**
   ```
   Authorization: Bearer <your_jwt_token>
   ```

## API Endpoints

### 1. Create Address
**Endpoint:** `POST /api/v1/addresses`

**Request Body:**
```json
{
  "streetAddress1": "123 Nguyen Van Linh",
  "streetAddress2": "Floor 5, Room 501",
  "ward": "Phuong 7",
  "district": "Quan 7",
  "city": "Ho Chi Minh",
  "postalCode": "700000",
  "countryCode": "VN",
  "contactFullName": "Nguyen Van A",
  "contactPhoneNumber": "0123456789",
  "addressType": "HOME",
  "isDefaultShipping": true,
  "isDefaultBilling": false
}
```

**Expected Response:**
```json
{
  "statusCode": 201,
  "message": "Address created successfully",
  "data": {
    "id": 1,
    "streetAddress1": "123 Nguyen Van Linh",
    "streetAddress2": "Floor 5, Room 501",
    "ward": "Phuong 7",
    "district": "Quan 7",
    "city": "Ho Chi Minh",
    "postalCode": "700000",
    "countryCode": "VN",
    "contactFullName": "Nguyen Van A",
    "contactPhoneNumber": "0123456789",
    "addressType": "HOME",
    "isDefaultShipping": true,
    "isDefaultBilling": false,
    "fullAddress": "123 Nguyen Van Linh, Floor 5, Room 501, Phuong 7, Quan 7, Ho Chi Minh, 700000, VN"
  }
}
```

### 2. Get User Addresses
**Endpoint:** `GET /api/v1/addresses`

**Expected Response:**
```json
{
  "statusCode": 200,
  "message": "Addresses retrieved successfully",
  "data": [
    {
      "id": 1,
      "streetAddress1": "123 Nguyen Van Linh",
      "streetAddress2": "Floor 5, Room 501",
      "ward": "Phuong 7",
      "district": "Quan 7",
      "city": "Ho Chi Minh",
      "postalCode": "700000",
      "countryCode": "VN",
      "contactFullName": "Nguyen Van A",
      "contactPhoneNumber": "0123456789",
      "addressType": "HOME",
      "isDefaultShipping": true,
      "isDefaultBilling": false,
      "fullAddress": "123 Nguyen Van Linh, Floor 5, Room 501, Phuong 7, Quan 7, Ho Chi Minh, 700000, VN"
    }
  ]
}
```

### 3. Get Address by ID
**Endpoint:** `GET /api/v1/addresses/{addressId}`

**Expected Response:** Same as single address object above

### 4. Update Address
**Endpoint:** `PUT /api/v1/addresses/{addressId}`

**Request Body:**
```json
{
  "streetAddress2": "Floor 6, Room 601",
  "contactPhoneNumber": "0987654321",
  "isDefaultShipping": false
}
```

**Expected Response:** Updated address object

### 5. Set Default Shipping Address
**Endpoint:** `PATCH /api/v1/addresses/{addressId}/default-shipping`

**Expected Response:** Address object with `isDefaultShipping: true`

### 6. Set Default Billing Address
**Endpoint:** `PATCH /api/v1/addresses/{addressId}/default-billing`

**Expected Response:** Address object with `isDefaultBilling: true`

### 7. Get Default Shipping Address
**Endpoint:** `GET /api/v1/addresses/default-shipping`

**Expected Response:** Default shipping address object

### 8. Get Default Billing Address
**Endpoint:** `GET /api/v1/addresses/default-billing`

**Expected Response:** Default billing address object

### 9. Delete Address
**Endpoint:** `DELETE /api/v1/addresses/{addressId}`

**Expected Response:**
```json
{
  "statusCode": 200,
  "message": "Address deleted successfully"
}
```

## Test Scenarios

### Scenario 1: Complete Address Lifecycle
1. Create address with `isDefaultShipping: true`
2. Verify it appears in user addresses list
3. Verify it's set as default shipping
4. Update address details
5. Set another address as default shipping
6. Verify first address is no longer default
7. Delete the address

### Scenario 2: Multiple Addresses
1. Create 3 different addresses
2. Set different default flags for each
3. Verify only one can be default shipping
4. Verify only one can be default billing
5. Test address listing order (newest first)

### Scenario 3: Address Validation
1. Try to create address with missing required fields
2. Try to update non-existent address
3. Try to delete default address
4. Try to delete only address
5. Verify proper error messages

### Scenario 4: Integration with Orders
1. Create address
2. Use address ID in checkout request
3. Verify address snapshot is stored in order
4. Verify order creation works

## Error Handling

### Common Error Responses

**400 Bad Request:**
```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "errors": [
    "Street address 1 is required",
    "District is required"
  ]
}
```

**404 Not Found:**
```json
{
  "statusCode": 404,
  "message": "Address not found with id: 999"
}
```

**403 Forbidden:**
```json
{
  "statusCode": 403,
  "message": "Access denied: JWT expired"
}
```

## Notes
- All addresses are user-specific (users can only access their own addresses)
- Address snapshots are stored in orders for historical reference
- Default address management ensures only one default per type per user
- Address deletion has business rules (can't delete only address, can't delete default)
