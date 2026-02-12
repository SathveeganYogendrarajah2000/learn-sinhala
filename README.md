# Learn Sinhala

A full-stack Sinhala learning application for Tamil speakers.

> 🎵 **Fun fact**: This entire project was vibe coded! 🚀

## Tech Stack

- **Frontend**: Angular 19 + Nginx
- **Backend**: Spring Boot 3 + Java 21
- **Database**: MongoDB Atlas
- **Auth**: JWT
- **Hosting**: Google Cloud Run
- **Registry**: DockerHub

## Architecture

```
┌─────────────────────┐
│   User's Browser    │
└──────────┬──────────┘
           │
     ┌─────┴──────┐
     │             │
     ▼             ▼
┌──────────┐  ┌──────────┐
│ Frontend │  │ Backend  │   ← Separate Cloud Run services
│ (Nginx)  │  │ (Spring) │
└──────────┘  └────┬─────┘
                   │
                   ▼
            ┌──────────────┐
            │ MongoDB Atlas│   ← Cloud Database
            └──────────────┘
```

The frontend calls the backend API directly via its Cloud Run URL (no nginx proxy in production).

## Features

- ✅ User authentication (JWT)
- ✅ Vocabulary management (CRUD + CSV upload)
- ✅ Practice sessions with audio
- ✅ Sentence builder
- ✅ Progress tracking & statistics

## Project Structure

```
.
├── backend/                    # Spring Boot API
│   ├── src/                   # Java source code
│   ├── Dockerfile             # Backend container
│   └── pom.xml                # Maven dependencies
├── frontend/                   # Angular SPA
│   ├── src/                   # TypeScript source
│   ├── nginx.conf.template    # Nginx config (envsubst at runtime)
│   └── Dockerfile             # Frontend container
├── .env.example               # Local dev environment template
├── .env.prod.example          # Cloud Run env vars reference
└── README.md
```

---

## Deployment Guide (Google Cloud Run)

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed
- [Google Cloud CLI (`gcloud`)](https://cloud.google.com/sdk/docs/install) installed
- A [DockerHub](https://hub.docker.com/) account
- A [Google Cloud](https://console.cloud.google.com/) project with Cloud Run enabled
- MongoDB Atlas cluster running (see [MongoDB Atlas](https://cloud.mongodb.com/))

### Step 1: Update the Backend API URL

Before building the frontend, update the backend URL in `frontend/src/environments/environment.prod.ts`:

```typescript
apiUrl: 'https://YOUR-BACKEND-SERVICE-URL.run.app/api',
```

> ⚠️ You'll get this URL **after** deploying the backend (Step 4). You may need to build & deploy the frontend **twice** — first to get the backend URL, then again with the correct URL.

### Step 2: Build Docker Images

```bash
# Build backend
docker build -t sathveegan/learn-sinhala-backend:v1.0.0 ./backend

# Build frontend
docker build -t sathveegan/learn-sinhala-frontend:v1.0.0 ./frontend
```

### Step 3: Push to DockerHub

```bash
# Login to DockerHub
docker login

# Push images
docker push sathveegan/learn-sinhala-backend:v1.0.0
docker push sathveegan/learn-sinhala-frontend:v1.0.0
```

### Step 4: Deploy Backend to Cloud Run

**Option A: Using `gcloud` CLI**

```bash
gcloud run deploy learn-sinhala-backend \
  --image docker.io/sathveegan/learn-sinhala-backend:v1.0.0 \
  --platform managed \
  --region asia-south1 \
  --allow-unauthenticated \
  --port 8080 \
  --memory 512Mi \
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" \
  --set-env-vars "SPRING_DATA_MONGODB_URI=mongodb+srv://USER:PASS@cluster.mongodb.net/learnsinhala-prod?retryWrites=true&w=majority&authSource=admin" \
  --set-env-vars "JWT_SECRET=your-generated-jwt-secret" \
  --set-env-vars "JWT_EXPIRATION=86400000" \
  --set-env-vars "CORS_ALLOWED_ORIGINS=https://your-frontend-url.run.app"
```

**Option B: Using Cloud Console UI**

1. Go to [Cloud Run Console](https://console.cloud.google.com/run)
2. Click **"Create Service"**
3. Enter image URL: `docker.io/sathveegan/learn-sinhala-backend:v1.0.0`
4. Set region (e.g. `asia-south1`)
5. Under **"Container, Networking, Security"**:
   - Port: `8080`
   - Memory: `512 Mi`
6. Under **"Variables & Secrets"**, add:

   | Variable | Value |
   |----------|-------|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `SPRING_DATA_MONGODB_URI` | `mongodb+srv://...` |
   | `JWT_SECRET` | *(generate with `openssl rand -base64 64`)* |
   | `JWT_EXPIRATION` | `86400000` |
   | `CORS_ALLOWED_ORIGINS` | `https://your-frontend-url.run.app` |

7. Under **"Authentication"**, select **"Allow unauthenticated invocations"**
8. Click **"Create"**

Copy the service URL (e.g. `https://learn-sinhala-backend-xxxxx.run.app`).

### Step 5: Deploy Frontend to Cloud Run

```bash
gcloud run deploy learn-sinhala-frontend \
  --image docker.io/sathveegan/learn-sinhala-frontend:v1.0.0 \
  --platform managed \
  --region asia-south1 \
  --allow-unauthenticated \
  --port 8080 \
  --memory 128Mi
```

Or use the Cloud Console UI (same steps as backend, but **no environment variables needed**).

### Step 6: Update CORS & Rebuild Frontend

After both services are deployed:

1. **Update CORS** on the backend — edit the `CORS_ALLOWED_ORIGINS` env var to match the frontend Cloud Run URL
2. **Update `environment.prod.ts`** with the actual backend URL
3. **Rebuild & redeploy** the frontend:
   ```bash
   docker build -t sathveegan/learn-sinhala-frontend:v1.0.1 ./frontend
   docker push sathveegan/learn-sinhala-frontend:v1.0.1
   gcloud run deploy learn-sinhala-frontend \
     --image docker.io/sathveegan/learn-sinhala-frontend:v1.0.1 \
     --region asia-south1
   ```

---

## Local Development

```bash
# Backend (requires Java 21 + Maven)
cd backend
mvn spring-boot:run

# Frontend (requires Node 20)
cd frontend
npm install
ng serve
```

Access at: http://localhost:4200 (frontend) / http://localhost:8080/api (backend)

## Environment Variables Reference

See [.env.prod.example](./.env.prod.example) for the full list of Cloud Run environment variables.

## License

Private - All Rights Reserved

## Author

Built with ☕ and 🎵 vibes
