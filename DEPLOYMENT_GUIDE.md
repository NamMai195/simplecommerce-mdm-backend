# 🚀 SimpleCommerce MDM Backend - Deployment Guide

## 📋 Mục Lục
- [Tổng Quan Kiến Trúc](#tổng-quan-kiến-trúc)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Quick Start](#quick-start)
- [Môi Trường Development](#môi-trường-development)
- [Môi Trường Production](#môi-trường-production)
- [Hướng Dẫn Team](#hướng-dẫn-team)
- [Troubleshooting](#troubleshooting)

## 🏗️ Tổng Quan Kiến Trúc

### Chiến Lược: Modular Monolith → Microservices

**Lý do chọn Modular Monolith:**
- ✅ **Khởi đầu nhanh:** Phát triển và debug đơn giản
- ✅ **Chi phí thấp:** Ít phức tạp về infrastructure
- ✅ **Thiết kế sẵn sàng:** Dễ dàng tách thành microservices sau này
- ✅ **Transaction đơn giản:** Không cần distributed transaction

**Quy Tắc Thiết Kế:**
1. **Module độc lập:** Mỗi package là một bounded context
2. **Giao tiếp qua interface:** Không truy cập trực tiếp database của module khác
3. **Shared model:** Chỉ chia sẻ common entities và DTOs
4. **Event-driven:** Sử dụng application events cho giao tiếp giữa modules

### Module Structure
```
src/main/java/com/simplecommerce_mdm/
├── auth/           # Authentication & Authorization
├── user/           # User Management  
├── product/        # Product Catalog Management
├── order/          # Order Processing
├── cart/           # Shopping Cart
├── promotion/      # Promotions & Vouchers
├── review/         # Product Reviews
└── common/         # Shared Components
    ├── domain/     # Base entities
    ├── enums/      # Shared enums
    └── controller/ # Health checks
```

## 📁 Cấu Trúc Dự Án

### Files Đã Được Tạo

```
simplecommerce-mdm-backend/
├── src/main/resources/
│   ├── application.yml           # Cấu hình chung + profile activation
│   ├── application-dev.yml       # Development environment
│   └── application-prod.yml      # Production environment
├── Dockerfile                    # Multi-stage Docker build
├── docker-compose.yml           # Development environment
├── docker-compose.prod.yml      # Production environment  
├── .dockerignore                # Docker build optimization
├── env.example                  # Environment variables template
├── Makefile                     # Project management commands
└── DEPLOYMENT_GUIDE.md          # This guide
```

## 🚀 Quick Start

### 1. Clone và Setup
```bash
# Clone repository
git clone <repository-url>
cd simplecommerce-mdm-backend

# Copy environment file
cp env.example .env
# Edit .env với các giá trị thực tế

# Xem tất cả commands có sẵn
make help
```

### 2. Development Environment
```bash
# Start development environment (PostgreSQL + App + pgAdmin + Redis)
make dev

# Kiểm tra status
make status

# Xem logs
make dev-logs

# Stop environment
make dev-stop
```

### 3. Truy Cập Services
- **Application:** http://localhost:8080
- **Health Check:** http://localhost:8080/api/v1/health  
- **pgAdmin:** http://localhost:5050 (admin@simplecommerce.com / admin123)
- **PostgreSQL:** localhost:5432 (nammai / Nam@0917174910)

## 🔧 Môi Trường Development

### Cấu Hình (application-dev.yml)
- **Database:** PostgreSQL với connection pooling
- **Logging:** Debug level với SQL logging
- **JPA:** `ddl-auto: update` for development convenience
- **Monitoring:** Health endpoints enabled

### Commands Thường Dùng
```bash
# Development
make dev                # Start everything
make dev-logs          # Follow logs
make dev-stop          # Stop services
make dev-clean         # Clean volumes và restart fresh

# Database
make db-connect        # Connect to database
make db-backup         # Backup database

# Maven (local development)
make maven-run         # Run without Docker
make maven-test        # Run tests
make maven-package     # Build JAR file
```

### Hot Reload
Ứng dụng sử dụng Spring Boot DevTools để hot reload trong development.

## 🏭 Môi Trường Production

### Cấu Hình (application-prod.yml)
- **Database:** Optimized connection pooling
- **Security:** Environment variables cho sensitive data
- **Logging:** Structured logging với file output
- **Monitoring:** Prometheus metrics enabled
- **JPA:** `ddl-auto: none` (no auto schema changes)

### Services Include
- **Application:** Multiple replicas với health checks
- **PostgreSQL:** Production-optimized configuration
- **Redis:** Authentication enabled
- **Nginx:** Reverse proxy với SSL support
- **Prometheus:** Monitoring và metrics

### Deployment Commands
```bash
# Build production image
make prod-build

# Start production environment
make prod-up

# Monitor production
make prod-logs

# Stop production
make prod-stop
```

### Environment Variables
Copy `env.example` to `.env` và cập nhật:
- Database credentials
- JWT secrets (minimum 64 characters)
- Redis password
- External service API keys

## 👥 Hướng Dẫn Team

### 1. Quy Tắc Development

#### A. Model Development
- **Sử dụng BaseEntity:** Tất cả entities extend `BaseEntity`
- **Soft Delete:** Sử dụng `@SQLDelete` và `@Where` annotations
- **Lazy Loading:** Default `FetchType.LAZY` cho relationships
- **Builder Pattern:** Sử dụng Lombok `@Builder` với `@Builder.Default`

#### B. Module Communication
```java
// ✅ ĐÚNG: Giao tiếp qua Service interface
@Service
public class OrderService {
    private final UserService userService; // Interface
    private final ProductService productService; // Interface
}

// ❌ SAI: Truy cập trực tiếp repository của module khác
@Service  
public class OrderService {
    private final UserRepository userRepository; // Không được phép
}
```

#### C. Database Migration
- Sử dụng Flyway hoặc Liquibase cho production
- Development có thể dùng `ddl-auto: update`
- Backup database trước khi migration: `make db-backup`

### 2. Git Workflow
```bash
# Feature development
git checkout -b feature/user-authentication
# ... development ...
git add .
git commit -m "feat: implement user authentication"
git push origin feature/user-authentication
```

### 3. Testing Strategy
```bash
# Run tests
make maven-test

# Run with coverage
make ci-test

# Integration testing với Docker
make dev
# Run integration tests
make maven-test
make dev-stop
```

## 🔧 Troubleshooting

### Common Issues

#### 1. Port Already in Use
```bash
# Check port usage
lsof -i :8080
lsof -i :5432

# Kill process
kill -9 <PID>
```

#### 2. Database Connection Issues
```bash
# Check database container
docker ps | grep postgres

# Check database logs
docker logs simplecommerce-db

# Connect manually
make db-connect
```

#### 3. Permission Issues
```bash
# Fix Docker permissions
sudo chmod 666 /var/run/docker.sock

# Fix file permissions
sudo chown -R $USER:$USER .
```

#### 4. Out of Memory
```bash
# Check Docker resources
docker system df

# Clean unused resources
make clean-all
```

### Logging Locations
- **Development:** Container logs (`make dev-logs`)
- **Production:** `/app/logs/simplecommerce-mdm.log`
- **Database:** PostgreSQL container logs

### Health Checks
```bash
# Application health
curl http://localhost:8080/api/v1/health

# Database health
make db-connect

# Using Makefile
make health
```

## 📈 Next Steps

### 1. CI/CD Pipeline
- Setup GitHub Actions hoặc GitLab CI
- Automated testing và deployment
- Container registry integration

### 2. Monitoring & Observability
- ELK Stack integration
- Grafana dashboards
- Application performance monitoring

### 3. Security Enhancements
- Security headers
- Rate limiting
- API authentication middleware

### 4. Microservices Migration
- Event sourcing implementation
- API Gateway integration
- Service mesh consideration

## 🆘 Support

### Team Resources
- **Documentation:** `/docs` folder
- **API Documentation:** http://localhost:8080/swagger-ui.html
- **Database Schema:** pgAdmin (http://localhost:5050)
- **Monitoring:** Prometheus + Grafana (khi setup)

### Commands Reference
```bash
make help              # Show all available commands
make dev              # Start development
make prod-up          # Start production  
make db-backup        # Backup database
make clean-all        # Clean everything
```

---

**📝 Note:** Document này sẽ được cập nhật thường xuyên. Hãy đảm bảo bạn đang sử dụng phiên bản mới nhất. 