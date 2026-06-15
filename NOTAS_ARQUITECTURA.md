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
- `review-service`: creacion de resenas validando compra pagada.
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
- La seguridad por roles esta pendiente de completar en todos los microservicios distintos a auth-service.

### Variable de entorno JWT_SECRET (obligatoria en produccion)

El `auth-service` lee el secret JWT desde la variable de entorno `JWT_SECRET`.
Para desarrollo local, crear un archivo `.env` en la raiz del proyecto (ya ignorado por `.gitignore`):

```env
JWT_SECRET=mi_clave_secreta_desarrollo_local_minimo_32_caracteres_!!
```

Para produccion (Docker, Kubernetes, etc.):

```bash
# Docker run
docker run -e JWT_SECRET=<clave_produccion_segura> auth-service

# Docker Compose: agregar en auth-service environment:
environment:
  JWT_SECRET: ${JWT_SECRET}
```

**NUNCA** commitear la clave real de produccion en el repositorio.

## 8) Bugs corregidos (auth-service) - Mayo 2026

Se corrigieron los siguientes problemas de seguridad y logica en `auth-service`:

| # | Archivo | Problema | Fix aplicado |
|---|---|---|---|
| 1 | `JwtUtil.java` | `getBytes()` sin charset causaba tokens invalidos entre SO | `getBytes(StandardCharsets.UTF_8)` |
| 2 | `AuthController.java` | El cliente podia enviar `roles: ["ADMIN"]` al registrarse | Roles siempre forzados a `USER` en el servidor |
| 3 | `AuthController.java` | `/users/{u}/seller` sin autenticacion: cualquiera podia escalar privilegios | `@PreAuthorize("hasRole('ADMIN')")` |
| 4 | `AuthController.java` | `/users/{username}` exponia datos de cualquier usuario sin autenticacion | `@PreAuthorize("isAuthenticated()")` |
| 5 | `AuthController.java` | `login()` con credenciales incorrectas retornaba HTTP 500 | Captura `AuthenticationException` -> 401 |
| 6 | `AuthController.java` | `register()` sin `@Transactional`: race condition posible con registros simultaneos | `@Transactional` + catch `DataIntegrityViolationException` -> 409 |
| 7 | `RegisterRequest.java` | Campo `roles` en el DTO permitia que el cliente lo enviara | Campo `roles` eliminado del DTO |
| 8 | `GlobalExceptionHandler.java` | `RuntimeException` no manejada causaba 500 generico sin mensaje util | Handler especifico: "no encontrado" -> 404, resto -> 500 |
| 9 | `GlobalExceptionHandler.java` | `AuthenticationException` y `AccessDeniedException` no manejadas | Handlers con 401 y 403 respectivamente |
| 10 | `SecurityConfig.java` | Todo `/api/auth/**` era publico, exponiendo endpoints admin | Rutas granulares: solo `/register`, `/login`, `/validate` son publicos |
| 11 | `application.properties` | `jwt.secret` hardcodeado en texto plano en el repositorio | Secret leido desde variable de entorno `JWT_SECRET` |

## 9) Endurecimiento de seguridad - Junio 2026

Se completo la capa de seguridad que estaba pendiente ("FALTA CONFIRMAR" del V6.0):

| # | Area | Problema | Fix aplicado |
|---|---|---|---|
| 1 | Config | `jwt.secret` solo existia en `auth-service`; el resto referenciaba `${jwt.secret}` sin definirlo (no podian validar tokens y los valores ni coincidian) | Secret centralizado en `config-repo/application.properties` (servido por Config Server a todos), leido de `JWT_SECRET`. Quitado el hardcode de `auth-service.properties` |
| 2 | `payment-service` | `SecurityConfig` con `anyRequest().permitAll()`: cualquiera podia crear/consultar pagos sin token | `JwtService` + `JwtAuthenticationFilter` (deps jjwt). Todo exige JWT; el webhook de Mercado Pago queda publico |
| 3 | `review-service` | `permitAll()` total | Filtro JWT. POST resena requiere autenticacion; GET resenas publico |
| 4 | `messaging-service` | `permitAll()` total: las conversaciones privadas quedaban expuestas | Filtro JWT; todo requiere autenticacion |
| 5 | `notification-service` | `permitAll()` total | Filtro JWT; todo requiere autenticacion |
| 6 | `recommendation-service` | `permitAll()` total | Filtro JWT; todo requiere autenticacion |
| 7 | `product-service` | Roles autenticados pero nunca autorizados: cualquier USER podia crear/borrar productos y categorias | `@PreAuthorize` en escrituras: productos crear/editar/borrar -> `SELLER` o `ADMIN`; categorias -> `ADMIN` |

### Pendientes / notas

- El `api-gateway` mantiene `permitAll()` a proposito: cada microservicio valida su propio JWT (modelo descentralizado). Si se quiere validacion centralizada, anadir un filtro JWT en el gateway.
- El claim `privilegios` que leen los filtros aun no lo emite `auth-service` en `JwtUtil.generateToken` (solo emite `roles`). Si se van a usar privilegios finos, hay que anadirlo al token.
- `order-service` ya exigia autenticacion; sus endpoints de cambio de estado los consume `payment-service` via REST, por eso no se restringieron a un rol (romperia la llamada entre servicios sin propagacion de token).
