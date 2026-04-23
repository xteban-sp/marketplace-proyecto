# Notas de arquitectura e integracion

Estas notas cubren la arquitectura backend integrada: Eureka, Gateway, microservicios, bases de datos, Kafka, Redis y flujo base de pago con Mercado Pago.

## 1) Decision tomada para bases de datos

Se dejo una distribucion poliglota (sin Oracle):

- PostgreSQL:
  - `auth-service`
  - `order-service`
  - `messaging-service`
  - `notification-service`
  - `recommendation-service`
  - `review-service`
- MySQL:
  - `product-service`
  - `payment-service`

Con esto se cumple el requisito de usar MySQL y PostgreSQL en multiples microservicios.

## 2) Cambios de integracion entre microservicios

- Eureka Server habilitado y configurado en puerto `8761`.
- API Gateway configurado en `8080` con rutas a:
  - `/api/auth/**`
  - `/api/products/**`
  - `/api/categories/**`
  - `/api/orders/**`
  - `/api/payments/**`
  - `/api/messages/**`
  - `/api/notifications/**`
  - `/api/recommendations/**`
  - `/api/reviews/**`
- Los microservicios quedaron registrados como clientes de Eureka con `defaultZone` comun.

## 3) Estado funcional actual backend

- `auth-service`: registro/login/validacion JWT.
- `product-service`: CRUD de productos y categorias, busqueda y cache con Redis.
- `order-service`: creacion y consulta de ordenes, actualizacion de estado de pago, evento `order-created`.
- `payment-service`: creacion de pago, preferencia de Mercado Pago (sandbox/fallback), webhook de estado, eventos `payment-approved` y `payment-failed`.
- `messaging-service`: envio de mensajes y consulta por conversacion.
- `notification-service`: consulta y marcado de notificaciones, consumo de eventos Kafka.
- `review-service`: creacion de reseñas validando compra pagada.
- `recommendation-service`: recomendaciones simples por usuario con cache Redis y consumo de eventos.

## 4) Puertos de servicios

- `api-gateway`: `8080`
- `auth-service`: `8081`
- `product-service`: `8082`
- `order-service`: `8083`
- `payment-service`: `8084`
- `messaging-service`: `8085`
- `notification-service`: `8086`
- `recommendation-service`: `8087`
- `review-service`: `8088`
- `eureka-server`: `8761`

## 5) Docker Compose de infraestructura

Archivo: `docker-compose.yml`

Servicios incluidos:

- `postgres-auth` (puerto host `5432`)
- `postgres-order` (puerto host `5433`)
- `postgres-messaging` (puerto host `5434`)
- `postgres-notification` (puerto host `5435`)
- `postgres-recommendation` (puerto host `5436`)
- `postgres-review` (puerto host `5437`)
- `mysql-product` (puerto host `3306`)
- `mysql-payment` (puerto host `3307`)
- `zookeeper` (puerto host `2181`)
- `kafka` (puerto host `9092`)
- `redis` (puerto host `6379`)

Objetivo: levantar la infraestructura completa de soporte para que los microservicios se conecten localmente sin Oracle.

## 6) Validaciones recomendadas

1. Levantar infraestructura:

```bash
docker compose up -d
```

2. Levantar `eureka-server`, luego microservicios y finalmente `api-gateway`.

3. Validar Eureka:

- Abrir `http://localhost:8761`
- Deben aparecer: `API-GATEWAY`, `AUTH-SERVICE`, `PRODUCT-SERVICE`, `ORDER-SERVICE`, `PAYMENT-SERVICE`, `MESSAGING-SERVICE`, `NOTIFICATION-SERVICE`, `RECOMMENDATION-SERVICE`, `REVIEW-SERVICE`

4. Validar rutas de Gateway (si los controladores ya existen):

- `http://localhost:8080/api/auth/...`
- `http://localhost:8080/api/products/...`
- `http://localhost:8080/api/orders/...`
- `http://localhost:8080/api/payments/...`
- `http://localhost:8080/api/messages/...`
- `http://localhost:8080/api/notifications/...`
- `http://localhost:8080/api/recommendations?...`
- `http://localhost:8080/api/reviews?...`

## 7) Notas de seguridad y ajustes futuros

- Las credenciales actuales son solo de desarrollo local.
- Para produccion, mover secretos a variables de entorno seguras y rotar tokens.
- El acceso token de Mercado Pago debe inyectarse con `MERCADOPAGO_ACCESS_TOKEN`.
- Falta cerrar seguridad por roles en todos los servicios (hoy se permite para acelerar integracion).
