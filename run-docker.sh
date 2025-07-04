#!/bin/bash

# ==============================================
# SimpleCommerce MDM Backend - Docker Runner
# ==============================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
COMPOSE_FILE="docker-compose.yml"
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
    log_info "Kiểm tra requirements..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker chưa được cài đặt!"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_warning "docker-compose chưa được cài đặt, sử dụng 'docker compose'"
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    if [ ! -f "$ENV_FILE" ]; then
        log_warning "File .env không tồn tại, tạo từ env.example..."
        cp env.example .env
        log_info "Vui lòng cập nhật các giá trị trong file .env"
    fi
    
    log_success "Requirements đã đầy đủ!"
}

build_images() {
    log_info "Building Docker images..."
    $COMPOSE_CMD -f $COMPOSE_FILE build --no-cache
    log_success "Build hoàn thành!"
}

start_services() {
    log_info "Starting services..."
    
    # Tạo network nếu chưa có
    docker network create ${PROJECT_NAME}-network 2>/dev/null || true
    
    # Tạo các thư mục cần thiết
    mkdir -p data/{postgres,redis,pgadmin,logs}
    chmod 755 data/*
    
    # Start services
    $COMPOSE_CMD -f $COMPOSE_FILE up -d
    
    log_success "Services đã được khởi động!"
    show_status
}

stop_services() {
    log_info "Stopping services..."
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
    $COMPOSE_CMD -f $COMPOSE_FILE ps
    
    echo ""
    log_info "Service URLs:"
    echo "🌐 API Backend: http://localhost:$(grep SERVER_PORT .env | cut -d'=' -f2 | head -1)"
    echo "🗄️  pgAdmin: http://localhost:$(grep PGLADMIN_PORT .env | cut -d'=' -f2 | head -1)"
    echo "💾 Redis: localhost:$(grep REDIS_PORT .env | cut -d'=' -f2 | head -1)"
    echo "🐘 PostgreSQL: localhost:$(grep POSTGRES_PORT .env | cut -d'=' -f2 | head -1)"
}

show_logs() {
    service=${1:-""}
    if [ -z "$service" ]; then
        log_info "Showing logs cho tất cả services..."
        $COMPOSE_CMD -f $COMPOSE_FILE logs -f
    else
        log_info "Showing logs cho service: $service"
        $COMPOSE_CMD -f $COMPOSE_FILE logs -f $service
    fi
}

cleanup() {
    log_warning "Cleaning up containers, volumes, and networks..."
    read -p "Bạn có chắc muốn xóa tất cả data? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        $COMPOSE_CMD -f $COMPOSE_FILE down -v --remove-orphans
        docker network rm ${PROJECT_NAME}-network 2>/dev/null || true
        sudo rm -rf data/
        log_success "Cleanup hoàn thành!"
    else
        log_info "Cleanup bị hủy."
    fi
}

show_help() {
    echo "==============================================
SimpleCommerce MDM Backend - Docker Runner
==============================================

Usage: $0 <command>

Commands:
  build      - Build Docker images
  start      - Start all services  
  stop       - Stop all services
  restart    - Restart all services
  status     - Show service status and URLs
  logs       - Show logs (specify service name or leave empty for all)
  cleanup    - Remove all containers, volumes, and data
  help       - Show this help message

Examples:
  $0 start                # Start all services
  $0 logs api            # Show API logs
  $0 logs                # Show all logs
  $0 cleanup             # Remove everything

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