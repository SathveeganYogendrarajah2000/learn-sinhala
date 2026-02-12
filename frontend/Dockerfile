# ===========================================
# Learn Sinhala - Frontend Dockerfile
# Multi-stage build: Node for build, Nginx for serve
# ===========================================

# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files first (better layer caching)
COPY package*.json ./

# Install dependencies
RUN npm ci --legacy-peer-deps

# Build configuration: 'production' for Cloud Run, 'development' for local
ARG BUILD_CONFIG=production

# Copy source code
COPY . .

# Build with the specified configuration
RUN npm run build -- --configuration=${BUILD_CONFIG}

# Stage 2: Serve with Nginx
FROM nginx:alpine AS runtime

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy nginx template (will be processed by entrypoint)
COPY nginx.conf.template /etc/nginx/conf.d/nginx.conf.template

# Copy entrypoint script and fix Windows line endings
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN sed -i 's/\r$//' /docker-entrypoint.sh && chmod +x /docker-entrypoint.sh

# Copy built Angular app from builder stage
COPY --from=builder /app/dist/learn-sinhala/browser /usr/share/nginx/html

# Add non-root user for security
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

# Cloud Run injects PORT env var (default 8080)
ENV PORT=8080
EXPOSE ${PORT}

# Use custom entrypoint
ENTRYPOINT ["/docker-entrypoint.sh"]
