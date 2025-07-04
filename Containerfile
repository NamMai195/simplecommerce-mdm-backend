# Containerfile for SimpleCommerce MDM Backend
# Multi-stage build for optimal image size and security

# ==============================================
# Stage 1: Build Dependencies Cache
# ==============================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS deps
WORKDIR /app

# Copy dependency files for caching
COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn/

# Make mvnw executable and download dependencies
RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B --no-transfer-progress

# ==============================================
# Stage 2: Build Application
# ==============================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy cached dependencies
COPY --from=deps /root/.m2 /root/.m2
COPY --from=deps /app/pom.xml ./
COPY --from=deps /app/mvnw ./
COPY --from=deps /app/.mvn .mvn/

# Copy source code
COPY src/ src/

# Build application
RUN chmod +x mvnw && \
    ./mvnw clean package -DskipTests -B --no-transfer-progress && \
    mv target/*.jar app.jar

# ==============================================
# Stage 3: Runtime Image
# ==============================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Accept build args
ARG JAVA_OPTS="-Xmx512m -Xms256m"
ENV JAVA_OPTS=$JAVA_OPTS

# Install necessary packages
RUN apk add --no-cache \
    curl \
    ca-certificates \
    tzdata && \
    update-ca-certificates

# Set timezone to Asia/Ho_Chi_Minh
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create non-root user for security
RUN addgroup -g 1001 -S simplecommerce && \
    adduser -u 1001 -S simplecommerce -G simplecommerce

# Set working directory
WORKDIR /app

# Create required directories with proper permissions
RUN mkdir -p /app/logs /app/temp && \
    chown -R simplecommerce:simplecommerce /app

# Copy application jar from build stage
COPY --from=build --chown=simplecommerce:simplecommerce /app/app.jar ./app.jar

# Switch to non-root user
USER simplecommerce

# Expose port
EXPOSE 8080

# Set JVM options for containerized environment
ENV JAVA_OPTS="\
    -Djava.security.egd=file:/dev/./urandom \
    -XX:+UseG1GC \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Dspring.output.ansi.enabled=ALWAYS \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Asia/Ho_Chi_Minh \
    $JAVA_OPTS"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/health || exit 1

# Set entry point with optimized JVM settings
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

# ==============================================
# Stage 4: Development Image (Optional)
# ==============================================
FROM runtime AS development

# Install additional development tools
USER root
RUN apk add --no-cache \
    git \
    bash \
    vim \
    htop

USER simplecommerce

# Set development environment
ENV SPRING_PROFILES_ACTIVE=dev
ENV SPRING_DEVTOOLS_RESTART_ENABLED=true

# Development entry point with debug options
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar"]

# ==============================================
# Labels for metadata
# ==============================================
LABEL maintainer="SimpleCommerce Team <dev@simplecommerce.com>"
LABEL version="1.0.0"
LABEL description="SimpleCommerce MDM Backend API"
LABEL org.opencontainers.image.source="https://github.com/your-org/simplecommerce-mdm-backend"
LABEL org.opencontainers.image.title="SimpleCommerce MDM Backend"
LABEL org.opencontainers.image.description="E-commerce Management Backend API built with Spring Boot" 