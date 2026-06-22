# ---- Build del SPA ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# La URL del API se inyecta en build (mismo dominio, vía Caddy -> /api)
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# ---- Servir con Caddy (HTTPS automático + reverse proxy) ----
FROM caddy:2-alpine
COPY --from=build /app/dist /srv
COPY Caddyfile /etc/caddy/Caddyfile
