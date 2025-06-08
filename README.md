# SimpleCommerce Backend

Chào mừng đến với project backend của nền tảng thương mại điện tử SimpleCommerce. Tài liệu này sẽ giúp các thành viên mới hiểu rõ về cấu trúc và các quy ước được sử dụng trong dự án.

## Công nghệ sử dụng

- **Ngôn ngữ:** Java 17+
- **Framework:** Spring Boot 3.x
- **Tương tác CSDL:** Spring Data JPA (Hibernate)
- **Bảo mật:** Spring Security
- **CSDL:** PostgreSQL (Production) / H2 (Development)
- **Build Tool:** Maven

---

## Kiến trúc hệ thống: Modular Monolith

Dự án được xây dựng theo kiến trúc **Monolithic được Module hóa (Modular Monolith)**.

#### Tại sao lại chọn kiến trúc này?

- **Đơn giản như Monolith:** Toàn bộ code nằm trong một project duy nhất, dễ dàng để chạy, kiểm thử và triển khai.
- **Tổ chức như Microservices:** Code được chia thành các module độc lập tương đối dựa trên nghiệp vụ (domain). Mỗi module có trách nhiệm riêng, giúp code rõ ràng, dễ bảo trì và giảm sự phụ thuộc lẫn nhau.
- **Dễ dàng mở rộng:** Khi cần, một module được thiết kế tốt có thể được tách ra thành một microservice riêng mà không ảnh hưởng lớn đến toàn bộ hệ thống.

---

## Cấu trúc thư mục

Đây là cấu trúc thư mục tổng thể của dự án. Việc tuân thủ cấu trúc này là bắt buộc để đảm bảo sự nhất quán.

simplecommerce-backend/
└── src/
└── main/
├── java/
│   └── com/
│       └── simplecommerce/
│           ├── auth/
│           ├── cart/
│           ├── common/
│           ├── config/
│           ├── exception/
│           ├── order/
│           ├── product/
│           ├── promotion/
│           ├── review/
│           ├── security/
│           └── user/
└── resources/
└── application.properties


### Chú giải các Module chính

Mỗi package trong `com.simplecommerce` đại diện cho một module chức năng hoặc một phần của cơ sở hạ tầng.

-   **`auth`**: Xử lý các nghiệp vụ **xác thực** như đăng ký, đăng nhập, tạo token.
-   **`user`**: Quản lý thông tin và các hoạt động liên quan đến người dùng (cập nhật profile, địa chỉ...).
-   **`product`**: Quản lý sản phẩm, danh mục, shop, và các thuộc tính liên quan.
-   **`cart`**: Xử lý logic giỏ hàng.
-   **`order`**: Xử lý logic đặt hàng, lịch sử đơn hàng và thanh toán.
-   **`promotion`**: Quản lý mã giảm giá, vouchers, và các chương trình khuyến mãi.
-   **`review`**: Quản lý đánh giá và bình luận sản phẩm.

---

### Chú giải các Package cơ sở hạ tầng

-   **`config`**: Chứa các lớp cấu hình `@Configuration` của Spring (ví dụ: tạo bean cho ModelMapper, PasswordEncoder...).
-   **`security`**: Chứa các cấu hình liên quan đến Spring Security, bộ lọc JWT và các logic bảo mật.
-   **`exception`**: Chứa các lớp xử lý exception tập trung (Global Exception Handler) để trả về response lỗi nhất quán.
-   **`common`**: Chứa các lớp tiện ích hoặc các đối tượng dùng chung cho toàn bộ dự án.

---

## Hướng dẫn Cài đặt & Khởi chạy

#### Yêu cầu
-   JDK 17 hoặc cao hơn.
-   Maven 3.8.x hoặc cao hơn.
-   IntelliJ IDEA hoặc IDE tương đương.

#### Các bước cài đặt
1.  **Clone a repository:**
    ```bash
    git clone <your-repository-url>
    ```
2.  **Mở project** bằng IntelliJ IDEA và chờ Maven tải các dependency về.

3.  **Cấu hình `application.properties`:**
    Mở file `src/main/resources/application.properties` và cấu hình kết nối CSDL.
    
    * **Để phát triển (dùng H2 In-memory):**
      ```properties
      spring.datasource.url=jdbc:h2:mem:simplecommerce_db
      spring.datasource.driverClassName=org.h2.Driver
      spring.datasource.username=sa
      spring.datasource.password=
      spring.h2.console.enabled=true
      spring.jpa.hibernate.ddl-auto=update
      ```

    * **Để chạy trên môi trường Staging/Production (dùng PostgreSQL):**
      ```properties
      spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
      spring.datasource.username=your_username
      spring.datasource.password=your_password
      spring.jpa.hibernate.ddl-auto=validate
      ```
4.  **Chạy ứng dụng:** Tìm đến file `SimplecommerceBackendApplication.java` và nhấn nút Run.

---

## Tài liệu API

Dự án tích hợp Springdoc OpenAPI để tự động tạo tài liệu. Sau khi chạy project, truy cập vào đường dẫn sau để xem và tương tác với các API:

[**http://localhost:8080/swagger-ui.html**](http://localhost:8080/swagger-ui.html)

---

## Nguyên tắc Code

-   **Luồng xử lý:** Luôn tuân thủ luồng `Controller -> Service -> Repository`.
-   **DTO Pattern:** Luôn sử dụng DTO (Data Transfer Object) để trao đổi dữ liệu giữa Controller và Service. Không bao giờ trả về Entity trực tiếp từ API.
-   **Validation:** Dùng `jakarta.validation` (ví dụ: `@NotBlank`, `@Email`) trên các DTO để kiểm tra dữ liệu đầu vào.
