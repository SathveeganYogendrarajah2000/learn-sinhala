# Learn Sinhala Application

A full-stack Sinhala learning application for Tamil speakers.

> 🎵 **Fun fact**: This entire project was vibe coded! 🚀

## Tech Stack

- **Frontend**: Angular + Nginx
- **Backend**: Spring Boot + MongoDB Atlas
- **Auth**: JWT
- **Deployment**: Docker + Docker Compose

## Quick Start

### Local Development

1. Copy environment file:
```bash
cp .env.example .env
```

2. Update `.env` with your credentials

3. Start all services:
```bash
docker-compose up
```

4. Access:
   - Frontend: http://localhost
   - Backend API: http://localhost:8080/api

### Production Deployment

See [ENV_SETUP.md](./ENV_SETUP.md) for detailed environment configuration.

1. Copy production environment:
```bash
cp .env.prod.example .env.prod
```

2. Update `.env.prod` with:
   - MongoDB Atlas connection string
   - Strong JWT secret (generate with `openssl rand -base64 64`)

3. Deploy:
```bash
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

4. Check status:
```bash
docker ps
docker logs -f learnsinhala-backend-prod
docker logs -f learnsinhala-frontend-prod
```

## Environment Variables

See:
- [.env.example](./.env.example) - Local development
- [.env.prod.example](./.env.prod.example) - Production
- [ENV_SETUP.md](./ENV_SETUP.md) - Detailed setup guide
- [MONGODB_ATLAS.md](./MONGODB_ATLAS.md) - MongoDB Atlas setup

**Critical for Production**:
- `MONGODB_URI`: MongoDB Atlas connection string
- `JWT_SECRET`: Generate with `openssl rand -base64 64`
- `BACKEND_VERSION` & `FRONTEND_VERSION`: Image version tags

## Features

- ✅ User authentication (JWT)
- ✅ Vocabulary management (CRUD)
- ✅ Practice sessions with audio
- ✅ Sentence builder
- ✅ Progress tracking & statistics
- ✅ Content Security Policy (Google Fonts support)

## Project Structure

```
.
├── backend/                    # Spring Boot API
│   ├── src/                   # Java source code
│   ├── Dockerfile             # Backend container
│   └── pom.xml                # Maven dependencies
├── frontend/                   # Angular SPA
│   ├── src/                   # TypeScript source
│   ├── nginx.conf             # Nginx config (dev)
│   └── Dockerfile             # Frontend container
├── docker/                     # Docker configurations
│   └── nginx-prod.conf        # Production nginx config
├── .env.example               # Local environment template
├── .env.prod.example          # Production template
├── docker-compose.yml         # Local dev setup
└── docker-compose.prod.yml    # Production setup
```

## Docker Commands

### Local Development
```bash
# Start all services
docker-compose up

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all
docker-compose down

# Remove volumes (fresh start)
docker-compose down -v
```

### Production
```bash
# Build and start
docker-compose -f docker-compose.prod.yml --env-file .env.prod up --build -d

# View logs
docker logs -f learnsinhala-backend-prod
docker logs -f learnsinhala-frontend-prod

# Stop services
docker-compose -f docker-compose.prod.yml --env-file .env.prod down

# Restart specific service
docker-compose -f docker-compose.prod.yml --env-file .env.prod restart frontend
```

### Building Docker Images

```bash
# Build backend
docker-compose -f docker-compose.prod.yml --env-file .env.prod build backend

# Build frontend
docker-compose -f docker-compose.prod.yml --env-file .env.prod build frontend

# Build both
docker-compose -f docker-compose.prod.yml --env-file .env.prod build
```

### Push to DockerHub

```bash
# Push backend
docker-compose -f docker-compose.prod.yml --env-file .env.prod push backend

# Push frontend
docker-compose -f docker-compose.prod.yml --env-file .env.prod push frontend

# Push both
docker-compose -f docker-compose.prod.yml --env-file .env.prod push
```

## Architecture

```
┌─────────────────────┐
│   User's Browser    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Nginx Container    │  ← Serves Angular + Proxies API
│  (Frontend)         │
└──────────┬──────────┘
           │ /api/*
           ▼
┌─────────────────────┐
│  Spring Boot        │  ← REST API
│  (Backend)          │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  MongoDB Atlas      │  ← Cloud Database
└─────────────────────┘
```

## Development Notes

- **Frontend**: Angular 19 with standalone components
- **Backend**: Spring Boot 3.2.1 with Java 21
- **Database**: MongoDB Atlas (production) or local MongoDB (dev)
- **Nginx**: Handles static files + API proxy (no CORS needed)
- **CSP**: Configured to allow Google Fonts while maintaining security

## CI/CD

GitHub Actions automatically:
- Builds on every push
- Runs tests
- Creates Docker images
- Deploys to production (on main branch)

## License

Private - All Rights Reserved

## Author

Built with ☕ and 🎵 vibes
