# Stage 1: Build application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only dependency files first for better caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (will be cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN mvn clean package -DskipTests -B

# Stage 2: Create final runtime image
FROM eclipse-temurin:17-jre-jammy AS runtime

# Install curl for healthcheck
USER root
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r simplecommerce && useradd -r -g simplecommerce simplecommerce

# Set working directory
WORKDIR /app

# Create logs directory
RUN mkdir -p /app/logs && chown -R simplecommerce:simplecommerce /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown simplecommerce:simplecommerce app.jar

# Switch to non-root user
USER simplecommerce

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", \
    "app.jar"] 