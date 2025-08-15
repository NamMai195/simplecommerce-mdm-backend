# Review System API Testing Guide

## Overview
Review System cung cấp đầy đủ chức năng quản lý đánh giá sản phẩm cho User, Seller và Admin.

## API Endpoints

### 1. User Review APIs (`/api/v1/reviews`)

#### 1.1 Create Review
```http
POST /api/v1/reviews
Authorization: Bearer {USER_TOKEN}
Content-Type: application/json

{
  "productId": 1,
  "orderId": 1,
  "rating": 5,
  "comment": "Sản phẩm rất tốt, giao hàng nhanh!"
}
```

#### 1.2 Update Review
```http
PUT /api/v1/reviews/{reviewId}
Authorization: Bearer {USER_TOKEN}
Content-Type: application/json

{
  "rating": 4,
  "comment": "Sản phẩm tốt, nhưng giao hàng hơi chậm"
}
```

#### 1.3 Delete Review
```http
DELETE /api/v1/reviews/{reviewId}
Authorization: Bearer {USER_TOKEN}
```

#### 1.4 Get Review Details
```http
GET /api/v1/reviews/{reviewId}
```

#### 1.5 Get Product Reviews
```http
GET /api/v1/reviews/product/{productId}?page=0&size=10&sortBy=createdAt&sortDir=desc
```

#### 1.6 Get User Reviews
```http
GET /api/v1/reviews/user?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer {USER_TOKEN}
```

#### 1.7 Get Product Review Statistics
```http
GET /api/v1/reviews/product/{productId}/statistics
```

#### 1.8 Mark Review as Helpful
```http
POST /api/v1/reviews/{reviewId}/helpful
Authorization: Bearer {USER_TOKEN}
```

#### 1.9 Report Review
```http
POST /api/v1/reviews/{reviewId}/report?reportReason=Inappropriate content
Authorization: Bearer {USER_TOKEN}
```

### 2. Admin Review APIs (`/api/v1/admin/reviews`)

#### 2.1 Get All Reviews
```http
GET /api/v1/admin/reviews?page=0&size=20&sortBy=createdAt&sortDir=desc
Authorization: Bearer {ADMIN_TOKEN}
```

#### 2.2 Get Review Details
```http
GET /api/v1/admin/reviews/{reviewId}
Authorization: Bearer {ADMIN_TOKEN}
```

#### 2.3 Get Pending Reviews
```http
GET /api/v1/admin/reviews/pending?page=0&size=20&sortBy=createdAt&sortDir=desc
Authorization: Bearer {ADMIN_TOKEN}
```

#### 2.4 Get Reported Reviews
```http
GET /api/v1/admin/reviews/reported?page=0&size=20&sortBy=createdAt&sortDir=desc
Authorization: Bearer {ADMIN_TOKEN}
```

#### 2.5 Moderate Review
```http
PUT /api/v1/admin/reviews/{reviewId}/moderate?isApproved=true&moderatorNotes=Review approved
Authorization: Bearer {ADMIN_TOKEN}
```

#### 2.6 Delete Review (Admin)
```http
DELETE /api/v1/admin/reviews/{reviewId}
Authorization: Bearer {ADMIN_TOKEN}
```

#### 2.7 Get Review Statistics Overview
```http
GET /api/v1/admin/reviews/statistics/overview
Authorization: Bearer {ADMIN_TOKEN}
```

### 3. Seller Review APIs (`/api/v1/seller/reviews`)

#### 3.1 Get Shop Reviews
```http
GET /api/v1/seller/reviews?page=0&size=20&sortBy=createdAt&sortDir=desc
Authorization: Bearer {SELLER_TOKEN}
```

#### 3.2 Get Product Reviews in Shop
```http
GET /api/v1/seller/reviews/product/{productId}?page=0&size=20&sortBy=createdAt&sortDir=desc
Authorization: Bearer {SELLER_TOKEN}
```

#### 3.3 Get Shop Review Statistics
```http
GET /api/v1/seller/reviews/statistics
Authorization: Bearer {SELLER_TOKEN}
```

#### 3.4 Get Product Review Statistics in Shop
```http
GET /api/v1/seller/reviews/product/{productId}/statistics
Authorization: Bearer {SELLER_TOKEN}
```

## Business Rules

### 1. Review Creation Rules
- ✅ **Only buyers can review** - Chỉ người mua mới được review
- ✅ **One review per order** - 1 review cho 1 order
- ✅ **Rating validation** - 1-5 sao
- ✅ **Order completion check** - Chỉ review sau khi order hoàn thành
- ✅ **Duplicate prevention** - Không cho review trùng lặp

### 2. Review Update Rules
- ✅ **Ownership check** - Chỉ chủ review mới được update
- ✅ **Rating validation** - 1-5 sao
- ✅ **Comment length** - Comment ≤ 1000 ký tự

### 3. Review Deletion Rules
- ✅ **Ownership check** - Chỉ chủ review mới được xóa
- ✅ **Admin override** - Admin có thể xóa bất kỳ review nào

### 4. Review Moderation Rules
- ✅ **Admin approval** - Review cần được admin approve
- ✅ **Content filtering** - Lọc nội dung không phù hợp
- ✅ **Report handling** - Xử lý review bị báo cáo

## Data Models

### 1. Review Entity
```java
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {
    private Long id;
    private User user;
    private Product product;
    private Order order;
    private Integer rating; // 1-5 stars
    private String comment;
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private Boolean isReported;
    private String reportReason;
    private Boolean isApproved;
    private String moderatorNotes;
}
```

### 2. DTOs
- **ReviewCreateRequest** - Tạo review mới
- **ReviewUpdateRequest** - Cập nhật review
- **ReviewResponse** - Response cho frontend
- **ReviewListResponse** - Danh sách reviews với pagination
- **ReviewStatisticsResponse** - Thống kê rating

## Testing Scenarios

### 1. User Review Flow
1. **Login as User** - Đăng nhập với tài khoản user
2. **Create Review** - Tạo review cho sản phẩm đã mua
3. **Update Review** - Chỉnh sửa review
4. **Delete Review** - Xóa review
5. **View Reviews** - Xem danh sách reviews

### 2. Admin Moderation Flow
1. **Login as Admin** - Đăng nhập với tài khoản admin
2. **View Pending Reviews** - Xem reviews chờ duyệt
3. **Moderate Review** - Duyệt/từ chối review
4. **Handle Reports** - Xử lý review bị báo cáo
5. **View Statistics** - Xem thống kê reviews

### 3. Seller Review Flow
1. **Login as Seller** - Đăng nhập với tài khoản seller
2. **View Shop Reviews** - Xem reviews của shop
3. **View Product Reviews** - Xem reviews của từng sản phẩm
4. **View Statistics** - Xem thống kê rating

## Error Handling

### 1. Validation Errors
- **Rating out of range** - Rating phải từ 1-5
- **Comment too long** - Comment không được quá 1000 ký tự
- **Missing required fields** - Thiếu thông tin bắt buộc

### 2. Business Rule Errors
- **User not buyer** - User không phải người mua
- **Already reviewed** - Đã review sản phẩm này
- **Order not completed** - Order chưa hoàn thành
- **Review not found** - Không tìm thấy review

### 3. Authorization Errors
- **Insufficient permissions** - Không đủ quyền
- **Review ownership** - Không phải chủ review
- **Role required** - Cần role cụ thể

## Performance Considerations

### 1. Pagination
- ✅ **Default page size** - 10 items per page
- ✅ **Configurable sorting** - Sắp xếp theo nhiều tiêu chí
- ✅ **Efficient queries** - Sử dụng JPA pagination

### 2. Caching
- ✅ **Review statistics** - Cache thống kê rating
- ✅ **Product ratings** - Cache rating của sản phẩm
- ✅ **User reviews** - Cache reviews của user

### 3. Database Optimization
- ✅ **Indexes** - Index trên các trường thường query
- ✅ **Lazy loading** - Lazy load relationships
- ✅ **Batch operations** - Xử lý batch khi cần

## Security Features

### 1. Role-Based Access Control
- ✅ **USER role** - Tạo, update, delete own reviews
- ✅ **SELLER role** - View shop reviews và statistics
- ✅ **ADMIN role** - Full access, moderation

### 2. Data Validation
- ✅ **Input sanitization** - Làm sạch input
- ✅ **Business rule validation** - Kiểm tra quy tắc nghiệp vụ
- ✅ **SQL injection prevention** - Ngăn chặn SQL injection

### 3. Audit Trail
- ✅ **Created/Updated timestamps** - Ghi nhận thời gian
- ✅ **User tracking** - Theo dõi user thực hiện
- ✅ **Moderation history** - Lịch sử kiểm duyệt

## Integration Points

### 1. Product Integration
- ✅ **Rating update** - Cập nhật rating sản phẩm
- ✅ **Review count** - Cập nhật số lượng reviews
- ✅ **Statistics calculation** - Tính toán thống kê

### 2. Order Integration
- ✅ **Purchase verification** - Xác minh đã mua hàng
- ✅ **Review status** - Đánh dấu order đã review
- ✅ **Order validation** - Kiểm tra order hợp lệ

### 3. User Integration
- ✅ **User authentication** - Xác thực user
- ✅ **Role verification** - Kiểm tra role
- ✅ **Ownership validation** - Xác minh quyền sở hữu

## Future Enhancements

### 1. Advanced Features
- **Review images** - Hình ảnh trong review
- **Review helpfulness** - Đánh giá review hữu ích
- **Review replies** - Trả lời review
- **Review moderation queue** - Hàng đợi kiểm duyệt

### 2. Analytics & Reporting
- **Review sentiment analysis** - Phân tích cảm xúc
- **Review trend analysis** - Phân tích xu hướng
- **Review quality metrics** - Chỉ số chất lượng review
- **Review fraud detection** - Phát hiện review giả

### 3. Performance Improvements
- **Redis caching** - Cache với Redis
- **Elasticsearch integration** - Tìm kiếm nâng cao
- **Async processing** - Xử lý bất đồng bộ
- **CDN integration** - Tối ưu delivery
