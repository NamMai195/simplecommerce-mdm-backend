# 🛒 SimpleCommerce MDM Backend

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![H2 Database](https://img.shields.io/badge/H2-Database-blue.svg)](https://www.h2database.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

> **Hệ thống Backend Thương mại điện tử** được xây dựng với Spring Boot, thiết kế theo kiến trúc **Modular Monolith** với các module được tổ chức rõ ràng.

## 📋 Mục Lục

- [🎯 Tổng Quan Dự Án](#-tổng-quan-dự-án)
- [🏗️ Kiến Trúc](#️-kiến-trúc)
- [🚀 Bắt Đầu Nhanh](#-bắt-đầu-nhanh)
- [⚙️ Cài Đặt Development](#️-cài-đặt-development)
- [🐳 Triển Khai Docker](#-triển-khai-docker)
- [📚 Tài Liệu API](#-tài-liệu-api)
- [🧪 Testing](#-testing)
- [🔧 Cấu Hình](#-cấu-hình)
- [🤝 Đóng Góp](#-đóng-góp)

## 🎯 Tổng Quan Dự Án

SimpleCommerce MDM (Master Data Management) Backend là nền tảng thương mại điện tử được thiết kế để quản lý:

- **Quản lý người dùng** - User, Role, Permission, Address
- **Catalog sản phẩm** - Product, Category, ProductVariant, Inventory
- **Xử lý đơn hàng** - Cart, Order, MasterOrder, Payment
- **Khuyến mãi** - Promotion, Voucher, UserAppliedVoucher
- **Đánh giá** - Review system

### 🎯 Tính Năng Hiện Tại

- ✅ **Kiến trúc Modular Monolith** - Các module được tổ chức rõ ràng
- ✅ **Entity Mapping hoàn chỉnh** - JPA entities với quan hệ đầy đủ
- ✅ **Audit Trail** - BaseEntity với created/updated tracking
- ✅ **Soft Delete Pattern** - Logical delete với deleted_at
- ✅ **JWT Authentication** - JWT token-based security
- ✅ **API Documentation** - OpenAPI 3 với Swagger UI
- ✅ **Multi-environment** - Dev, Test, Production profiles
- ✅ **Docker Support** - Full containerization với docker-compose
- ✅ **Database Flexibility** - H2 cho development, PostgreSQL cho production

## 🏗️ Kiến Trúc

### Cấu Trúc Module

```
src/main/java/com/simplecommerce_mdm/
├── 🔐 auth/           # Authentication & Authorization
│   ├── controller/    # AuthController
│   ├── dto/          # UserRegistrationDto
│   └── service/      # AuthService & Implementation
├── 👤 user/           # User Management
│   ├── model/        # User, Role, Permission, Address
│   └── repository/   # UserRepository
├── 📦 product/        # Product Catalog
│   └── model/        # Product, Category, ProductVariant, Shop
├── 🛒 cart/           # Shopping Cart
│   └── model/        # Cart, CartItem
├── 📋 order/          # Order Processing
│   └── model/        # MasterOrder, Order, OrderItem, Payment
├── 🎁 promotion/      # Promotions & Vouchers
│   └── model/        # Promotion, Voucher, UserAppliedVoucher
├── ⭐ review/         # Review System
│   └── model/        # Review
├── 🔧 config/         # Application Configuration
├── 🛡️ security/       # Security Configuration
└── 🌐 common/         # Shared Components
    ├── domain/        # BaseEntity với Audit
    ├── enums/         # Status enums
    └── controller/    # HealthController
```

### Stack Công Nghệ

| Loại | Công Nghệ |
|----------|------------|
| **Framework** | Spring Boot 3.5.0 |
| **Ngôn ngữ** | Java 17/21 |
| **Database** | H2 (Dev) / PostgreSQL 16 (Production) |
| **Caching** | Redis 7 (Docker only) |
| **Bảo mật** | Spring Security + JWT |
| **ORM** | Hibernate 6.x với JPA |
| **Build Tool** | Maven 3.9+ |
| **Containerization** | Docker & Docker Compose |
| **API Documentation** | SpringDoc OpenAPI 3 |
| **Logging** | SLF4J + Logback |

## 🚀 Bắt Đầu Nhanh

### Yêu Cầu Hệ Thống

- ☑️ **Java 17+** - [Tải OpenJDK](https://openjdk.org/)
- ☑️ **Maven 3.6+** - Hoặc sử dụng wrapper `./mvnw`
- ☑️ **Docker & Docker Compose** (Optional) - [Cài đặt Docker](https://docs.docker.com/get-docker/)

### 1. Clone Repository

```bash
git clone <repository-url>
cd simplecommerce-mdm-backend
```

### 2. Chạy ứng dụng (Development - H2 Database)

```bash
# Sử dụng Maven wrapper (Khuyến nghị)
./mvnw spring-boot:run

# Hoặc nếu đã cài Maven
mvn spring-boot:run
```

### 3. Truy cập ứng dụng

- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/api/v1/health
- **H2 Console**: http://localhost:8080/h2-console
- **API Documentation**: http://localhost:8080/swagger-ui.html

#### H2 Database Connection (Development)
```
URL: jdbc:h2:mem:simplecommerce_db
Username: sa
Password: (để trống)
```

## ⚙️ Cài Đặt Development

### Option 1: Local Development (H2 Database)

```bash
# Chạy với profile development (mặc định)
./mvnw spring-boot:run

# Hoặc với profile cụ thể
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Option 2: Docker Development (PostgreSQL)

```bash
# Khởi động full stack với PostgreSQL
make dev

# Hoặc sử dụng docker-compose trực tiếp
docker-compose up --build -d
```

### Truy Cập Các Dịch Vụ (Docker)

| Dịch vụ | URL | Thông tin đăng nhập |
|---------|-----|-------------|
| **Application** | http://localhost:8080 | - |
| **Health Check** | http://localhost:8080/api/v1/health | - |
| **API Docs** | http://localhost:8080/swagger-ui.html | - |
| **PostgreSQL** | localhost:5432 | nammai / Nam@0917174910 |
| **pgAdmin** | http://localhost:5050 | admin@simplecommerce.com / admin123 |
| **Redis** | localhost:6379 | - |

## 🐳 Triển Khai Docker

### Development Environment

```bash
# Khởi động tất cả dịch vụ
make dev

# Xem logs
make dev-logs

# Dừng dịch vụ
make dev-stop

# Dọn dẹp hoàn toàn
make dev-clean
```

### Production Environment

```bash
# Build production image
make prod-build

# Triển khai production
make prod-up

# Theo dõi logs
make prod-logs
```

### Dịch Vụ Trong Docker

- **Application** - Spring Boot app với PostgreSQL
- **PostgreSQL 16** - Database chính với persistent volume
- **Redis 7** - Caching layer (sẵn sàng tích hợp)
- **pgAdmin** - Database management interface

## 📚 Tài Liệu API

### Swagger UI
- **Development**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

### Endpoints Hiện Tại

```bash
# Health Check
GET /api/v1/health              # Application health status

# Authentication (In Development)
POST /api/v1/auth/register      # User registration
POST /api/v1/auth/login         # User login

# Note: Most endpoints are being developed
# Check Swagger UI for updated API documentation
```

## 🧪 Testing

### Chạy Tests

```bash
# Chạy tất cả tests
make maven-test
# hoặc
./mvnw test

# Chạy tests với coverage
make ci-test

# Chạy test cụ thể
./mvnw test -Dtest=SimpleEntityTest
```

### Test Configuration

- **Test Database**: H2 in-memory database
- **Test Profile**: Tự động sử dụng `test` profile
- **Test Coverage**: JaCoCo được cấu hình sẵn

## 🔧 Cấu Hình

### Environment Profiles

- **dev** (default) - H2 database, debug logging
- **test** - H2 in-memory, minimal logging  
- **prod** - PostgreSQL, optimized settings

### Database Configuration

#### Development (H2)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:simplecommerce_db
    driver-class-name: org.h2.Driver
    username: sa
    password:
```

#### Production (PostgreSQL via Docker)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/simplecommerce_mdm
    username: nammai
    password: Nam@0917174910
```

### Makefile Commands

```bash
make help           # Hiển thị tất cả commands
make dev            # Start development environment
make maven-run      # Run local với Maven
make maven-test     # Run tests
make health         # Check application health
make db-connect     # Connect to PostgreSQL
```

## 🤝 Đóng Góp

### Development Workflow

1. **Setup**: `make dev` hoặc `./mvnw spring-boot:run`
2. **Testing**: `make maven-test` trước khi commit
3. **Code Style**: Tuân thủ Java conventions
4. **Entity Design**: Extend `BaseEntity` cho audit trail
5. **Documentation**: Cập nhật API docs khi cần

### Project Structure Guidelines

```java
// ✅ Tốt: Entity với BaseEntity
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class User extends BaseEntity {
    // entity fields
}

// ✅ Tốt: Enum values
public enum OrderStatus {
    PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}
```

### Quy Ước Commit

```bash
feat: thêm User entity và repository
fix: sửa lỗi JSON mapping trong Product
docs: cập nhật README với cấu hình Docker
refactor: tối ưu hóa structure của Order module
```

---

## 📊 Trạng Thái Dự Án

### ✅ Đã Hoàn Thành
- Entity design và relationships
- Base architecture setup
- Docker containerization
- Development environment
- Basic authentication structure

### 🚧 Đang Phát Triển
- API endpoints implementation
- Service layer completion
- Frontend integration
- Advanced security features

### 📋 Kế Hoạch
- Redis caching integration
- File upload với Cloudinary
- Email notifications
- Performance monitoring
- CI/CD pipeline

---

## 📄 Giấy Phép

Dự án này đang trong quá trình phát triển cho mục đích học tập và thương mại.

---

<div align="center">

**🚀 SimpleCommerce MDM Backend - Nền tảng thương mại điện tử hiện đại**

[📚 API Docs](http://localhost:8080/swagger-ui.html) • [🐳 Docker Setup](docker-compose.yml) • [🔧 Makefile Commands](Makefile)

</div>
