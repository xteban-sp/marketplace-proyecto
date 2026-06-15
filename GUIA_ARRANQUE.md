# Guía de arranque y verificación

## 1. Levantar infraestructura

```bash
docker compose up -d
```

Esto levanta PostgreSQL (x6), MySQL (x2), Kafka, Zookeeper y Redis.

## 2. Levantar los servicios (en este orden)

1. `eureka-server`  (8761)
2. `config-service` (8888)
3. Microservicios (en cualquier orden): `auth` 8081, `product` 8082, `order` 8083,
   `payment` 8084, `messaging` 8085, `notification` 8086, `recommendation` 8087, `review` 8088
4. `api-gateway` (8080) — al final

Cada servicio: `./mvnw spring-boot:run` (necesita **Java 17+**).

Verifica en `http://localhost:8761` que aparezcan los 9 servicios `UP`.

## 3. Usuario ADMIN

Al arrancar, `auth-service` crea automáticamente un ADMIN si no existe:

- usuario: `admin`  ·  contraseña: `Admin12345!`  (cámbialos con `ADMIN_USERNAME` / `ADMIN_PASSWORD`)

## 4. Probar todo

- **Manual / visual**: abre `marketplace-api.http` en IntelliJ y ejecuta de arriba a abajo.
- **Automático**: `bash scripts/smoke-test.sh` (registro → login → producto → pedido → pago → webhook → reseña → notificación).

## 5. Mercado Pago (pago real en sandbox, gratis)

1. Crea cuenta y app en https://www.mercadopago.com.pe/developers
2. Copia el **Access Token de PRUEBA**.
3. Expónlo antes de arrancar `payment-service`:
   ```bash
   export MERCADOPAGO_ACCESS_TOKEN=TEST-xxxxxxxx
   ```
4. Crea **usuarios de prueba** (comprador y vendedor) y paga con **tarjetas de prueba** de la doc de MP.
   El dinero de prueba se mueve entre las cuentas de prueba, sin costo.

Sin token, `payment-service` funciona en modo simulado (confirmas con el webhook).

> Nota: para recibir webhooks reales de MP necesitas una URL pública
> (ej. `ngrok http 8084`) y ponerla en `MERCADOPAGO_NOTIFICATION_URL`.

## 6. Variables de entorno útiles (producción)

| Variable | Para qué |
|---|---|
| `JWT_SECRET` | Secret JWT compartido (mín. 32 chars) |
| `DDL_AUTO` | `validate` en prod (por defecto `update` en dev) |
| `SHOW_SQL` | `false` en prod |
| `LOG_LEVEL_SECURITY`, `LOG_LEVEL_APP` | `WARN`/`INFO` en prod |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | Credenciales del admin inicial |
| `CORS_ALLOWED_ORIGINS` | Orígenes del frontend (coma) |
| `MERCADOPAGO_ACCESS_TOKEN` | Token de Mercado Pago |
| `SEED_ENABLED` | `false` para no cargar datos de ejemplo |
