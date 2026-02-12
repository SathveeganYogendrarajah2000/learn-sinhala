#!/bin/sh

# Docker entrypoint script for Learn Sinhala Frontend
# Substitutes environment variables in nginx config at runtime

set -e

# Defaults (Cloud Run injects PORT; BACKEND_URL used in local docker-compose)
export PORT=${PORT:-8080}
export BACKEND_URL=${BACKEND_URL:-http://backend:8080}

echo "🚀 Starting Learn Sinhala Frontend..."
echo "🔌 Listening on port: $PORT"
echo "📡 Backend URL: $BACKEND_URL"

# Substitute environment variables in nginx config
envsubst '${PORT} ${BACKEND_URL}' < /etc/nginx/conf.d/nginx.conf.template > /etc/nginx/conf.d/default.conf

echo "✅ Nginx configuration generated"

# Start nginx
echo "🌐 Starting Nginx..."
exec nginx -g "daemon off;"
