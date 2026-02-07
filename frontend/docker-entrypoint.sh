#!/bin/sh

# Docker entrypoint script for Learn Sinhala Frontend
# Substitutes environment variables in nginx config at runtime

set -e

# Default backend URL if not provided
export BACKEND_URL=${BACKEND_URL:-http://backend:8080}

echo "🚀 Starting Learn Sinhala Frontend..."
echo "📡 Backend URL: $BACKEND_URL"

# Substitute environment variables in nginx config
envsubst '${BACKEND_URL}' < /etc/nginx/conf.d/nginx.conf.template > /etc/nginx/conf.d/default.conf

echo "✅ Nginx configuration generated"

# Start nginx
echo "🌐 Starting Nginx..."
exec nginx -g "daemon off;"
