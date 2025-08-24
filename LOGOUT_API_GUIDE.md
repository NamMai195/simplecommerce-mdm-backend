# Logout API Guide

## Overview

Chức năng logout đã được cải thiện với các tính năng bảo mật nâng cao:

- **Token Blacklisting**: Lưu trữ token đã logout vào database
- **SecurityContext Clear**: Xóa session trong memory
- **Token Validation**: Kiểm tra token hợp lệ trước khi logout
- **Automatic Cleanup**: Tự động xóa token hết hạn

## API Endpoints

### 1. Logout User

**POST** `/api/v1/auth/logout`

**Headers:**

```
Authorization: Bearer <access_token>
```

**Security:** Yêu cầu authentication với role USER, SELLER, hoặc ADMIN

**Response (200 OK):**

```json
{
  "statusCode": 200,
  "message": "Logout successful",
  "data": null
}
```

**Error Responses:**

- **400 Bad Request**: Invalid authorization header
- **401 Unauthorized**: Token không hợp lệ hoặc đã hết hạn
- **403 Forbidden**: User account bị deactivate
- **404 Not Found**: User không tồn tại

## Database Schema

### InvalidatedToken Table

```sql
CREATE TABLE invalidated_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(1000) NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL,
    token_type VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    invalidated_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_invalidated_token (token),
    INDEX idx_invalidated_user_email (user_email),
    INDEX idx_invalidated_expires (expires_at)
);
```

## Security Features

### 1. Token Blacklisting

- Token sau khi logout được lưu vào database
- Filter kiểm tra token blacklist trước mỗi request
- Token đã logout sẽ bị từ chối truy cập

### 2. SecurityContext Clear

- Xóa authentication context khỏi memory
- Đảm bảo session không còn tồn tại

### 3. Token Validation

- Kiểm tra token format và signature
- Validate user tồn tại và active
- Kiểm tra token chưa bị invalidate

### 4. Automatic Cleanup

- Scheduled task xóa token hết hạn mỗi giờ
- Giảm dung lượng database

## Workflow

### 1. User Logout Request

```
Client → POST /api/v1/auth/logout
Headers: Authorization: Bearer <token>
```

### 2. Server Validation

```
1. Validate authorization header format
2. Extract token và user info
3. Check user exists và active
4. Verify token chưa bị invalidate
```

### 3. Token Invalidation

```
1. Extract token expiration
2. Create InvalidatedToken record
3. Save to database
4. Clear SecurityContext
```

### 4. Response

```
200 OK: Logout successful
```

## Configuration

### Application Properties

```yaml
# JWT Configuration
jwt:
  accessKey: your-access-key
  refreshKey: your-refresh-key
  expiryMinutes: 30
  expiryDay: 7

# Cleanup Configuration
scheduling:
  invalidated-token-cleanup: 3600000 # 1 hour
```

## Testing

### cURL Example

```bash
curl -X POST "http://localhost:8080/api/v1/auth/logout" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### Postman Collection

```json
{
  "name": "Logout API",
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/api/v1/auth/logout",
    "headers": {
      "Authorization": "Bearer {{accessToken}}",
      "Content-Type": "application/json"
    }
  }
}
```

## Error Handling

### Common Errors

1. **Invalid Authorization Header**

   - Format không đúng: `Bearer <token>`
   - Token bị thiếu hoặc rỗng

2. **Token Already Invalidated**

   - Token đã được logout trước đó
   - Token đã hết hạn

3. **User Account Issues**

   - User không tồn tại
   - Account bị deactivate

4. **Token Validation Errors**
   - Token signature không hợp lệ
   - Token format không đúng

## Monitoring & Logging

### Log Messages

```
INFO  - Logout requested from user: user@example.com
INFO  - Successfully logged out user: user@example.com (ID: 123)
WARN  - Token already invalidated for user: user@example.com
ERROR - Error during logout: Token không hợp lệ
```

### Metrics

- Logout success rate
- Token invalidation count
- Cleanup performance
- Database size

## Best Practices

### 1. Client Implementation

- Gọi logout API khi user click logout
- Xóa token khỏi local storage
- Redirect về login page

### 2. Security Considerations

- Luôn sử dụng HTTPS
- Validate token format
- Implement rate limiting
- Monitor suspicious activities

### 3. Performance Optimization

- Index database columns
- Regular cleanup tasks
- Connection pooling
- Cache frequently accessed data

## Future Enhancements

### Phase 1 - Completed ✅

- [x] Token blacklisting
- [x] SecurityContext clear
- [x] Token validation
- [x] Automatic cleanup
- [x] Database schema
- [x] API documentation

### Phase 2 - Future 📋

- [ ] Refresh token invalidation
- [ ] Multi-device logout
- [ ] Logout history tracking
- [ ] Real-time notifications
- [ ] Advanced analytics
