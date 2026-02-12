# Google Cloud Run — Deployment Guide

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed
- [Google Cloud CLI](https://cloud.google.com/sdk/docs/install) installed
- [DockerHub](https://hub.docker.com/) account
- MongoDB Atlas cluster running

---

## 1. One-Time Setup

```bash
gcloud auth login
gcloud config set project YOUR_PROJECT_ID
gcloud services enable run.googleapis.com
```

---

## 2. Build & Push Images

```bash
# Build
docker build -t YOUR_DOCKERHUB_USER/learn-sinhala-backend:v1.0.0 ./backend
docker build -t YOUR_DOCKERHUB_USER/learn-sinhala-frontend:v1.0.0 ./frontend

# Push
docker login
docker push YOUR_DOCKERHUB_USER/learn-sinhala-backend:v1.0.0
docker push YOUR_DOCKERHUB_USER/learn-sinhala-frontend:v1.0.0
```

> The frontend Dockerfile defaults to `--configuration=production`, which uses `environment.prod.ts`. Make sure the `apiUrl` in that file points to your backend Cloud Run URL before building.

---

## 3. Deploy Backend

```bash
gcloud run deploy learn-sinhala-backend \
  --image docker.io/YOUR_DOCKERHUB_USER/learn-sinhala-backend:v1.0.0 \
  --platform managed \
  --region asia-south1 \
  --allow-unauthenticated \
  --port 8080 \
  --memory 512Mi \
  --cpu 1 \
  --max-instances 1 \
  --min-instances 0 \
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" \
  --set-env-vars "SPRING_DATA_MONGODB_URI=YOUR_MONGODB_ATLAS_URI" \
  --set-env-vars "JWT_SECRET=YOUR_JWT_SECRET" \
  --set-env-vars "JWT_EXPIRATION=86400000" \
  --set-env-vars "CORS_ALLOWED_ORIGINS=*"
```

Note the **Service URL** in the output — you'll need it next.

---

## 4. Deploy Frontend

> ⚠️ `BACKEND_URL` is **required**. Nginx uses it in `proxy_pass` and needs to resolve the hostname at startup. Without it, the container will crash.

```bash
gcloud run deploy learn-sinhala-frontend \
  --image docker.io/YOUR_DOCKERHUB_USER/learn-sinhala-frontend:v1.0.0 \
  --platform managed \
  --region asia-south1 \
  --allow-unauthenticated \
  --port 8080 \
  --memory 128Mi \
  --cpu 1 \
  --max-instances 1 \
  --min-instances 0 \
  --set-env-vars "BACKEND_URL=YOUR_BACKEND_CLOUD_RUN_URL"
```

---

## 5. Lock Down CORS

Update backend CORS to only allow your frontend URL:

```bash
gcloud run services update learn-sinhala-backend \
  --region asia-south1 \
  --update-env-vars "CORS_ALLOWED_ORIGINS=YOUR_FRONTEND_CLOUD_RUN_URL"
```

---

## 6. Verify

```bash
# Backend health
curl https://YOUR_BACKEND_URL.run.app/actuator/health
# Should return: {"status":"UP"}

# Open frontend in browser
# https://YOUR_FRONTEND_URL.run.app
```

---

## Deploying Updates

```bash
# Build, push, deploy
docker build -t YOUR_DOCKERHUB_USER/learn-sinhala-backend:v1.1.0 ./backend
docker push YOUR_DOCKERHUB_USER/learn-sinhala-backend:v1.1.0
gcloud run deploy learn-sinhala-backend --image docker.io/YOUR_DOCKERHUB_USER/learn-sinhala-backend:v1.1.0 --region asia-south1

docker build -t YOUR_DOCKERHUB_USER/learn-sinhala-frontend:v1.1.0 ./frontend
docker push YOUR_DOCKERHUB_USER/learn-sinhala-frontend:v1.1.0
gcloud run deploy learn-sinhala-frontend --image docker.io/YOUR_DOCKERHUB_USER/learn-sinhala-frontend:v1.1.0 --region asia-south1
```

---

## Environment Variables Reference

### Backend

| Variable | Description |
|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | Set to `prod` |
| `SPRING_DATA_MONGODB_URI` | MongoDB Atlas connection string |
| `JWT_SECRET` | Generate with `openssl rand -base64 64` |
| `JWT_EXPIRATION` | Token expiry in ms (default: `86400000` = 24h) |
| `CORS_ALLOWED_ORIGINS` | Frontend Cloud Run URL |

### Frontend

| Variable | Description |
|----------|-------------|
| `BACKEND_URL` | Backend Cloud Run URL (required for nginx startup) |

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Frontend "failed to start and listen on port" | Missing `BACKEND_URL` env var. Nginx can't resolve the proxy hostname at startup. |
| Backend can't connect to MongoDB | Check `SPRING_DATA_MONGODB_URI`. Ensure MongoDB Atlas has `0.0.0.0/0` in IP whitelist. |
| CORS errors in browser | Update `CORS_ALLOWED_ORIGINS` on backend to match frontend URL exactly. |
| Frontend shows network errors | Check `environment.prod.ts` has the correct backend URL. Rebuild & re-push. |

---

## Cost

Cloud Run with `min-instances=0` charges **only during active requests**. Free tier includes 2M requests/month — likely **$0** for personal use.
