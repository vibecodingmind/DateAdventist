# AdventHearts - Production Deployment Guide

## 1. Containerized Infrastructure (Docker)

```dockerfile
# Dockerfile for Backend API
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
COPY prisma ./prisma/
RUN npm ci
COPY . .
RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
COPY package*.json ./
RUN npm ci --only=production
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/prisma ./prisma
RUN npx prisma generate

EXPOSE 5000
CMD ["node", "dist/server.js"]
```

---

## 2. CI/CD Pipeline Steps
1. **Linting & Validation**: `npm run lint` & `npm run type-check`.
2. **Database Migration**: `npx prisma migrate deploy`.
3. **Container Build & Push**: Tag and push to Amazon ECR / Docker Hub.
4. **Deploy**: Kubernetes Rolling Update / AWS ECS Task definition update.
5. **Health Checks**: Poll `GET /health` to confirm 200 OK.
