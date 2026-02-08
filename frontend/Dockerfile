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

# Copy source code
COPY . .

# Build for production
RUN npm run build -- --configuration=production

# Stage 2: Serve with Nginx
FROM nginx:alpine AS runtime

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy nginx template (will be processed by entrypoint)
COPY nginx.conf.template /etc/nginx/conf.d/nginx.conf.template

# Copy entrypoint script
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Copy built Angular app from builder stage
COPY --from=builder /app/dist/learn-sinhala/browser /usr/share/nginx/html

# Add non-root user for security
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

# Environment variable with default value
ENV BACKEND_URL=http://backend:8080

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/health || exit 1

# Use custom entrypoint
ENTRYPOINT ["/docker-entrypoint.sh"]
