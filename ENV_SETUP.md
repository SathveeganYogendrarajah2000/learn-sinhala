# Environment Files Summary

## Local Development (`.env`)

✅ **Ready to use** - Already created with:
- Generated JWT secret
- Local MongoDB credentials
- Default ports

**Just run**: `docker-compose up`

## Production (`.env.prod.example` → `.env`)

For production deployment with MongoDB Atlas:

### Steps:

1. **Get MongoDB Atlas connection string**:
   - Sign up at https://cloud.mongodb.com/
   - Create free M0 cluster
   - Get connection string like:
     ```
     mongodb+srv://username:password@cluster.xxxxx.mongodb.net/learnsinhala
     ```

2. **On production server**:
   ```bash
   cp .env.prod.example .env
   nano .env
   ```

3. **Update these values**:
   ```bash
   # Paste your MongoDB Atlas connection string
   MONGODB_URI=mongodb+srv://...
   
   # Generate NEW secret (different from local!)
   JWT_SECRET=$(openssl rand -base64 64)
   ```

4. **Deploy**:
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

## Files Created

- ✅ `.env` - Local development (ready to use)
- ✅ `.env.prod.example` - Production template
- ✅ `MONGODB_ATLAS.md` - Detailed Atlas setup guide

## Key Differences

| Environment | MongoDB | File | Command |
|-------------|---------|------|---------|
| **Local** | Local container | `.env` | `docker-compose up` |
| **Production** | MongoDB Atlas | `.env` | `docker-compose -f docker-compose.prod.yml up -d` |

## Security

- ✅ `.env` is gitignored
- ✅ Different JWT secrets for local/prod
- ✅ MongoDB Atlas uses cloud authentication
