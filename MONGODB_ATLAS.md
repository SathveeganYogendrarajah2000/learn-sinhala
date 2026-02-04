# Production Deployment with MongoDB Atlas

## Environment Setup

### 1. Get MongoDB Atlas Connection String

1. Go to [MongoDB Atlas](https://cloud.mongodb.com/)
2. Create a free cluster (M0 Sandbox)
3. Create database user (Database Access)
4. Whitelist IP addresses (Network Access → Add IP: `0.0.0.0/0` for all IPs)
5. Get connection string:
   - Click "Connect" → "Connect your application"
   - Copy the connection string
   - Format: `mongodb+srv://username:password@cluster.xxxxx.mongodb.net/learnsinhala?retryWrites=true&w=majority`

### 2. Create Production Environment File

On your production server:

```bash
cd /path/to/learn-sinhala

# Copy the production template
cp .env.prod.example .env

# Edit with your values
nano .env
```

Update these values in `.env`:

```bash
# Your MongoDB Atlas connection string
MONGODB_URI=mongodb+srv://YOUR_USERNAME:YOUR_PASSWORD@your-cluster.mongodb.net/learnsinhala?retryWrites=true&w=majority

# Generate a NEW JWT secret (different from local!)
# Run: openssl rand -base64 64
JWT_SECRET=<paste-generated-secret-here>

# Leave these as default
JWT_EXPIRATION=86400000
SPRING_PROFILE=prod
BACKEND_PORT=8080
FRONTEND_PORT=80
```

### 3. Deploy

```bash
# Pull latest code
git pull origin main

# Deploy with production config
docker-compose -f docker-compose.prod.yml up -d --build

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f
```

## Production vs Local

### Local Development (`.env`)
- Uses local MongoDB container
- Variables: `MONGO_USER`, `MONGO_PASSWORD`, `MONGO_ROOT_USER`, `MONGO_ROOT_PASSWORD`
- Command: `docker-compose up`

### Production (`.env` on server)
- Uses MongoDB Atlas (cloud)
- Variable: `MONGODB_URI` (connection string from Atlas)
- Command: `docker-compose -f docker-compose.prod.yml up -d`

## Important Notes

1. **Never commit `.env` to git** - it contains secrets
2. **Use different JWT secrets** for local and production
3. **MongoDB Atlas free tier** (M0) has limitations:
   - 512 MB storage
   - Shared RAM
   - No backup
   - Good for learning/small apps

## Troubleshooting

### Connection Issues

If backend can't connect to MongoDB Atlas:

1. **Check IP whitelist**: Atlas → Network Access → Add `0.0.0.0/0`
2. **Verify connection string**: Check username, password, cluster name
3. **Check logs**: `docker-compose -f docker-compose.prod.yml logs backend`

### Environment Variables

```bash
# Verify env vars are loaded
docker-compose -f docker-compose.prod.yml config
```

## Security Checklist

- [ ] `.env` file is NOT committed to git
- [ ] Strong, unique JWT secret generated
- [ ] MongoDB Atlas user has strong password
- [ ] IP whitelist configured (or use specific IPs)
- [ ] Database name is correct (`learnsinhala`)
