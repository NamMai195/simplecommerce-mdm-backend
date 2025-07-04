#!/bin/bash

# ==============================================
# SimpleCommerce MDM Backend - Podman Runner
# ==============================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
COMPOSE_FILE="podman-compose.yml"
ENV_FILE=".env"
PROJECT_NAME="simplecommerce"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_requirements() {
    log_info "Kiểm tra requirements cho Podman..."
    
    if ! command -v podman &> /dev/null; then
        log_error "Podman chưa được cài đặt!"
        log_info "Cài đặt Podman:"
        log_info "  Fedora/RHEL: sudo dnf install podman"
        log_info "  Ubuntu: sudo apt install podman"
        exit 1
    fi
    
    # Check for podman-compose
    if command -v podman-compose &> /dev/null; then
        COMPOSE_CMD="podman-compose"
        log_success "Sử dụng podman-compose"
    elif command -v docker-compose &> /dev/null; then
        COMPOSE_CMD="docker-compose"
        log_warning "Sử dụng docker-compose với podman"
    else
        log_error "Không tìm thấy podman-compose hoặc docker-compose!"
        log_info "Cài đặt podman-compose:"
        log_info "  pip3 install podman-compose"
        exit 1
    fi
    
    if [ ! -f "$ENV_FILE" ]; then
        log_warning "File .env không tồn tại, tạo từ env.example..."
        cp env.example .env
        log_info "Vui lòng cập nhật các giá trị trong file .env"
    fi
    
    # Check if podman is rootless
    if [ "$(id -u)" = "0" ]; then
        log_warning "Đang chạy với root privileges"
    else
        log_info "Đang chạy rootless podman (recommended)"
    fi
    
    log_success "Requirements đã đầy đủ!"
}

setup_podman() {
    log_info "Setting up Podman environment..."
    
    # Enable podman socket for docker-compose compatibility
    if ! systemctl --user is-active --quiet podman.socket; then
        log_info "Enabling podman socket..."
        systemctl --user enable --now podman.socket
    fi
    
    # Set DOCKER_HOST for docker-compose compatibility
    export DOCKER_HOST="unix:///run/user/$(id -u)/podman/podman.sock"
    
    # Create network if not exists
    podman network create ${PROJECT_NAME}-network 2>/dev/null || true
    
    log_success "Podman environment ready!"
}

build_images() {
    log_info "Building Podman images..."
    setup_podman
    
    # Build với cache và buildah backend
    BUILDAH_FORMAT=docker $COMPOSE_CMD -f $COMPOSE_FILE build --no-cache
    log_success "Build hoàn thành!"
}

start_services() {
    log_info "Starting services với Podman..."
    setup_podman
    
    # Tạo các thư mục cần thiết với permissions phù hợp
    mkdir -p data/{postgres,redis,pgladmin,logs}
    
    # Set SELinux context cho volumes (nếu có SELinux)
    if command -v setsebool &> /dev/null; then
        log_info "Setting SELinux context for volumes..."
        sudo setsebool -P container_manage_cgroup on 2>/dev/null || true
        chcon -Rt container_file_t data/ 2>/dev/null || true
    fi
    
    # Set permissions
    chmod 755 data/*
    
    # Start services
    $COMPOSE_CMD -f $COMPOSE_FILE up -d
    
    log_success "Services đã được khởi động với Podman!"
    show_status
}

stop_services() {
    log_info "Stopping services..."
    setup_podman
    $COMPOSE_CMD -f $COMPOSE_FILE down
    log_success "Services đã được dừng!"
}

restart_services() {
    log_info "Restarting services..."
    stop_services
    sleep 2
    start_services
}

show_status() {
    log_info "Service status:"
    setup_podman
    $COMPOSE_CMD -f $COMPOSE_FILE ps
    
    echo ""
    log_info "Podman containers:"
    podman ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    echo ""
    log_info "Service URLs:"
    echo "🌐 API Backend: http://localhost:$(grep SERVER_PORT .env | cut -d'=' -f2 | head -1)"
    echo "🗄️  pgAdmin: http://localhost:$(grep PGADMIN_PORT .env | cut -d'=' -f2 | head -1)"
    echo "💾 Redis: localhost:$(grep REDIS_PORT .env | cut -d'=' -f2 | head -1)"
    echo "🐘 PostgreSQL: localhost:$(grep POSTGRES_PORT .env | cut -d'=' -f2 | head -1)"
}

show_logs() {
    setup_podman
    service=${1:-""}
    if [ -z "$service" ]; then
        log_info "Showing logs cho tất cả services..."
        $COMPOSE_CMD -f $COMPOSE_FILE logs -f
    else
        log_info "Showing logs cho service: $service"
        $COMPOSE_CMD -f $COMPOSE_FILE logs -f $service
    fi
}

podman_info() {
    log_info "Thông tin Podman system:"
    podman system info
    echo ""
    log_info "Podman volumes:"
    podman volume ls
    echo ""
    log_info "Podman networks:"
    podman network ls
}

cleanup() {
    log_warning "Cleaning up Podman containers, volumes, and networks..."
    read -p "Bạn có chắc muốn xóa tất cả data? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        setup_podman
        $COMPOSE_CMD -f $COMPOSE_FILE down -v --remove-orphans
        
        # Clean up podman resources
        podman system prune -a -f --volumes
        podman network rm ${PROJECT_NAME}-network 2>/dev/null || true
        
        # Remove data directory
        rm -rf data/
        
        log_success "Cleanup hoàn thành!"
    else
        log_info "Cleanup bị hủy."
    fi
}

show_help() {
    echo "==============================================
SimpleCommerce MDM Backend - Podman Runner
==============================================

Usage: $0 <command>

Commands:
  build      - Build Podman images
  start      - Start all services with Podman
  stop       - Stop all services
  restart    - Restart all services
  status     - Show service status and URLs
  logs       - Show logs (specify service name or leave empty for all)
  info       - Show Podman system information
  cleanup    - Remove all containers, volumes, and data
  help       - Show this help message

Examples:
  $0 start                # Start all services
  $0 logs api            # Show API logs
  $0 logs                # Show all logs
  $0 info                # Show podman info
  $0 cleanup             # Remove everything

Podman-specific features:
  - Rootless containers (safer)
  - SELinux support with :Z volume mounts
  - Better systemd integration
  - No daemon required

==============================================
"
}

# Main execution
case "$1" in
    "build")
        check_requirements
        build_images
        ;;
    "start")
        check_requirements
        start_services
        ;;
    "stop")
        stop_services
        ;;
    "restart")
        restart_services
        ;;
    "status")
        show_status
        ;;
    "logs")
        show_logs $2
        ;;
    "info")
        podman_info
        ;;
    "cleanup")
        cleanup
        ;;
    "help"|"--help"|"-h"|"")
        show_help
        ;;
    *)
        log_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac 