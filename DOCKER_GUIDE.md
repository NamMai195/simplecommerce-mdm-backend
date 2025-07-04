# 🐳 Hướng Dẫn Docker & Podman - SimpleCommerce MDM Backend

## 📋 Tổng Quan

Project này hỗ trợ cả **Docker** và **Podman** với các file cấu hình được tối ưu:

- `docker-compose.yml` - Cho Docker
- `podman-compose.yml` - Cho Podman (rootless, SELinux support)
- `Containerfile` - Multi-stage build với Alpine Linux
- `run-docker.sh` - Script tiện ích cho Docker
- `run-podman.sh` - Script tiện ích cho Podman

## 🏗️ Kiến Trúc Services

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Spring Boot   │    │   PostgreSQL    │    │     Redis       │
│   (Port 8080)   │◄──►│   (Port 5432)   │    │   (Port 6379)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         ▲
         │
┌─────────────────┐
│     pgAdmin     │
│   (Port 5050)   │
└─────────────────┘
```

## 🚀 Sử Dụng Với Docker

### 1. Cài Đặt Requirements

```bash
# Ubuntu/Debian
sudo apt update && sudo apt install docker.io docker-compose

# Fedora/RHEL
sudo dnf install docker docker-compose

# Start Docker service
sudo systemctl enable --now docker
sudo usermod -aG docker $USER  # Log out and back in
```

### 2. Chạy Với Script

```bash
# Sử dụng script tiện ích
./run-docker.sh help           # Xem help
./run-docker.sh start          # Start tất cả services
./run-docker.sh status         # Check status
./run-docker.sh logs api       # Xem logs API
./run-docker.sh stop           # Stop services
```

### 3. Chạy Manual

```bash
# Build images
docker-compose build

# Start services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f api

# Stop services
docker-compose down
```

## 🔧 Sử Dụng Với Podman

### 1. Cài Đặt Requirements

```bash
# Fedora/RHEL/CentOS
sudo dnf install podman podman-compose

# Ubuntu (20.04+)
sudo apt update && sudo apt install podman
pip3 install podman-compose

# Enable rootless mode
systemctl --user enable --now podman.socket
```

### 2. Chạy Với Script

```bash
# Sử dụng script tiện ích cho Podman
./run-podman.sh help           # Xem help
./run-podman.sh start          # Start tất cả services
./run-podman.sh status         # Check status  
./run-podman.sh info           # Podman system info
./run-podman.sh logs api       # Xem logs API
./run-podman.sh stop           # Stop services
```

### 3. Chạy Manual

```bash
# Setup environment
export DOCKER_HOST="unix:///run/user/$(id -u)/podman/podman.sock"
systemctl --user start podman.socket

# Build và start
podman-compose -f podman-compose.yml build
podman-compose -f podman-compose.yml up -d

# Check status
podman ps
podman-compose -f podman-compose.yml ps
```

## ⚙️ Cấu Hình Environment Variables

File `.env` sẽ được tự động tạo từ `env.example` nếu chưa có:

```bash
# Các biến quan trọng cần cấu hình:
SERVER_PORT=8080
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_password
POSTGRES_DB=simplecommerce_mdm
JWT_SECRET_KEY=your_jwt_secret_key_64_characters_minimum
REDIS_PASSWORD=your_redis_password
```

## 📂 Persistent Data

Dữ liệu được lưu trong thư mục `./data/`:

```
data/
├── postgres/    # PostgreSQL data
├── redis/       # Redis data
├── pgadmin/     # pgAdmin settings
└── logs/        # Application logs
```

## 🌐 Service URLs

Sau khi start thành công:

- **🌐 API Backend**: http://localhost:8080
  - Swagger UI: http://localhost:8080/swagger-ui.html
  - Health Check: http://localhost:8080/api/v1/health

- **🗄️ pgAdmin**: http://localhost:5050
  - Email: `admin@simplecommerce.com` (từ `.env`)
  - Password: Từ `PGLADMIN_DEFAULT_PASSWORD`

- **💾 Redis**: localhost:6379
- **🐘 PostgreSQL**: localhost:5432

## 🔍 Troubleshooting

### Docker Issues

```bash
# Check Docker service
sudo systemctl status docker

# Fix permission issues
sudo usermod -aG docker $USER
newgrp docker

# Clean up resources
docker system prune -a
./run-docker.sh cleanup
```

### Podman Issues

```bash
# Check podman socket
systemctl --user status podman.socket

# Fix rootless issues
podman system reset
./run-podman.sh cleanup

# SELinux context issues (Fedora/RHEL)
sudo setsebool -P container_manage_cgroup on
```

### Common Issues

1. **Port Already in Use**:
   ```bash
   # Check what's using the port
   sudo netstat -tlnp | grep :8080
   
   # Change port in .env
   SERVER_PORT=8081
   ```

2. **Database Connection Failed**:
   ```bash
   # Check database status
   docker-compose logs db
   podman-compose logs db
   
   # Reset database
   ./run-docker.sh stop
   sudo rm -rf data/postgres
   ./run-docker.sh start
   ```

3. **Build Failures**:
   ```bash
   # Clean build cache
   docker-compose build --no-cache
   podman-compose build --no-cache
   ```

## 🏗️ Development vs Production

### Development Build

```bash
# Build với development stage
docker build --target development -t simplecommerce-dev .
docker run -p 8080:8080 -p 5005:5005 simplecommerce-dev  # Debug port 5005
```

### Production Build

```bash
# Build production image
docker build --target runtime -t simplecommerce-prod .
docker run -p 8080:8080 simplecommerce-prod
```

## 📦 Image Details

**Multi-stage Containerfile**:
- **Stage 1**: Dependencies cache (Maven dependencies)
- **Stage 2**: Build application (compile source)
- **Stage 3**: Runtime (lightweight JRE)
- **Stage 4**: Development (với debug tools)

**Optimizations**:
- Alpine Linux base (nhẹ)
- Non-root user security
- G1GC garbage collector
- Proper timezone (Asia/Ho_Chi_Minh)
- Health checks
- Proper signal handling

## 🔐 Security Features

### Docker Security
- Non-root user trong container
- Read-only volumes where appropriate
- Network isolation
- Secret management qua environment variables

### Podman Security
- Rootless containers (không cần sudo)
- SELinux support với `:Z` mounts
- User namespace isolation
- No daemon security model

## 📊 Monitoring & Logs

```bash
# Real-time logs
./run-docker.sh logs api
./run-podman.sh logs api

# Application logs (persistent)
tail -f data/logs/simplecommerce-mdm.log

# Database logs
./run-docker.sh logs db

# All services logs
./run-docker.sh logs
```

## 🧹 Cleanup

```bash
# Cleanup Docker
./run-docker.sh cleanup

# Cleanup Podman  
./run-podman.sh cleanup

# Manual cleanup
docker-compose down -v --remove-orphans
podman-compose down -v --remove-orphans
```

---

## 🤝 Support

Nếu gặp vấn đề:

1. Check logs: `./run-docker.sh logs` hoặc `./run-podman.sh logs`
2. Verify environment: Đảm bảo file `.env` đúng
3. Check ports: Không có conflict với services khác
4. Clean restart: `./run-docker.sh cleanup` và start lại

**Happy coding! 🚀** 