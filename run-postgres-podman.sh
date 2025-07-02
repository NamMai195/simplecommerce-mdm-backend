#!/bin/bash

# ==============================================
# SimpleCommerce - PostgreSQL với Podman
# ==============================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Database configuration
DB_NAME="simplecommerce_mdm"
DB_USER="nammai"
DB_PASSWORD="Nam@0917174910"
DB_PORT="5432"
CONTAINER_NAME="simplecommerce-db"

echo -e "${BLUE}🐘 Starting PostgreSQL với Podman${NC}"
echo -e "${BLUE}=================================${NC}"

# Check if container already exists
if podman ps -a --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
    echo -e "${YELLOW}⚠️  Container ${CONTAINER_NAME} đã tồn tại${NC}"
    
    # Check if it's running
    if podman ps --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
        echo -e "${GREEN}✅ PostgreSQL đang chạy!${NC}"
    else
        echo -e "${YELLOW}🔄 Starting existing container...${NC}"
        podman start ${CONTAINER_NAME}
    fi
else
    echo -e "${YELLOW}🚀 Tạo container PostgreSQL mới...${NC}"
    
    # Run PostgreSQL container
    podman run -d \
        --name ${CONTAINER_NAME} \
        --restart unless-stopped \
        -e POSTGRES_USER=${DB_USER} \
        -e POSTGRES_PASSWORD=${DB_PASSWORD} \
        -e POSTGRES_DB=${DB_NAME} \
        -e POSTGRES_INITDB_ARGS="--encoding=UTF-8 --lc-collate=C --lc-ctype=C" \
        -p ${DB_PORT}:5432 \
        -v simplecommerce_data:/var/lib/postgresql/data:Z \
        docker.io/postgres:16-alpine
    
    echo -e "${GREEN}✅ PostgreSQL container đã được tạo!${NC}"
fi

# Wait for PostgreSQL to be ready
echo -e "${YELLOW}⏳ Đợi PostgreSQL khởi động...${NC}"
sleep 5

# Check if PostgreSQL is ready
while ! podman exec ${CONTAINER_NAME} pg_isready -U ${DB_USER} -d ${DB_NAME} > /dev/null 2>&1; do
    echo -e "${YELLOW}⏳ Đang đợi PostgreSQL...${NC}"
    sleep 2
done

echo -e "${GREEN}🎉 PostgreSQL đã sẵn sàng!${NC}"
echo -e "${BLUE}📋 Thông tin kết nối:${NC}"
echo -e "   Host: ${GREEN}localhost${NC}"
echo -e "   Port: ${GREEN}${DB_PORT}${NC}"
echo -e "   Database: ${GREEN}${DB_NAME}${NC}"
echo -e "   Username: ${GREEN}${DB_USER}${NC}"
echo -e "   Password: ${GREEN}${DB_PASSWORD}${NC}"
echo ""
echo -e "${BLUE}🔧 Useful commands:${NC}"
echo -e "   Connect to DB: ${YELLOW}podman exec -it ${CONTAINER_NAME} psql -U ${DB_USER} -d ${DB_NAME}${NC}"
echo -e "   View logs:     ${YELLOW}podman logs -f ${CONTAINER_NAME}${NC}"
echo -e "   Stop:          ${YELLOW}podman stop ${CONTAINER_NAME}${NC}"
echo -e "   Start:         ${YELLOW}podman start ${CONTAINER_NAME}${NC}"
echo ""
echo -e "${GREEN}✨ Bây giờ bạn có thể chạy Spring Boot app!${NC}"
echo -e "${YELLOW}./mvnw spring-boot:run -Dspring-boot.run.profiles=dev${NC}" 