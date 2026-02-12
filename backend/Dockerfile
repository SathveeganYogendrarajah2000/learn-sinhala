# ===========================================
# Learn Sinhala - Backend Dockerfile
# Multi-stage build for optimized image size
# ===========================================

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml first (better layer caching)
COPY pom.xml .

# Download dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime (lightweight JRE-only image)
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Add non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create storage directory
RUN mkdir -p /app/storage/audio && \
    chown -R appuser:appgroup /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Cloud Run injects PORT env var (default 8080)
ENV PORT=8080
EXPOSE ${PORT}

# JVM optimizations for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=$PORT"]
