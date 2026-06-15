# Plan: dejar el backend 100% listo para el frontend

Objetivo: que un SPA pueda registrarse, loguearse, navegar productos, comprar, pagar,
reseñar y recibir notificaciones de extremo a extremo, hablando **solo con el API Gateway**
(`http://localhost:8080`), sin tropezar con 401 internos, CORS ni datos rotos.

Estado base (auditoría 14/06/2026): la seguridad JWT ya quedó cerrada en todos los servicios
(ver `NOTAS_ARQUITECTURA.md` §9). Lo que sigue son los huecos que impiden que el flujo
funcione de verdad y que el frontend pueda integrarse sin fricción.

Leyenda de prioridad: 🔴 bloqueante · 🟠 importante · 🟡 mejora · ⚪ producción

---

## FASE 0 — Bloqueantes (sin esto NADA funciona de punta a punta)

### 0.1 🔴 Propagar el JWT en las llamadas entre servicios
**Problema:** ahora todos los servicios exigen JWT, pero los clientes Feign NO reenvían el token.
Estas llamadas internas devolverán **401**:

| Origen | Destino | Llamada |
|---|---|---|
| order-service | product-service | `GET /api/productos/{id}` (validar stock al crear pedido) |
| payment-service | order-service | `GET /api/pedidos/{id}` y `PATCH .../estado-pago` |
| review-service | order-service | `GET .../review-eligible` |

**Fix:** añadir un `RequestInterceptor` de Feign en order, payment y review que copie el header
`Authorization` de la petición entrante a la saliente. (product-service ya lo hace manual con `UserClient`.)

**Caso especial — webhook de pago:** el webhook lo llama Mercado Pago SIN token de usuario, y luego
payment llama a order. Ahí no hay token que reenviar. Opciones:
- (A) **Token de servicio interno**: un JWT de sistema con rol `SERVICE` que payment use para esa llamada. Más correcto.
- (B) **Permitir el endpoint interno** `PATCH /api/pedidos/{id}/estado-pago` solo desde la red interna. Más simple para demo.

### 0.2 🔴 CORS en el API Gateway
**Problema:** solo `auth-service` tiene CORS. El frontend entra por el gateway (:8080), que **no tiene CORS**
→ el navegador bloqueará todas las llamadas con error preflight.
**Fix:** configurar `CorsConfigurationSource` en el `api-gateway` (origen del SPA, métodos, headers, `Authorization`).
Al centralizarlo en el gateway, los servicios internos no necesitan CORS propio.

### 0.3 🔴 Bootstrap de usuario ADMIN
**Problema:** hay endpoints `hasRole('ADMIN')` (crear categorías, promover SELLER, borrar usuarios) pero
**no existe forma de crear el primer ADMIN** → deadlock (nadie puede gestionar nada).
**Fix:** `CommandLineRunner` en auth-service que cree un ADMIN inicial si no existe, con credenciales
por variable de entorno (`ADMIN_USERNAME` / `ADMIN_PASSWORD`). Idempotente.

---

## FASE 1 — Correctitud y consistencia (el flujo funciona pero con datos coherentes)

### 1.1 🟠 Inconsistencia de IDs: `Product.sellerId`
**Problema:** `User.id`, `Order.compradorId` y `Order.vendedorId` son **UUID**, pero `Product.id` y
`Product.sellerId` son **Long**. Un producto no puede guardar el UUID real de su vendedor, así que
"productos de este vendedor" ↔ "pedidos de este vendedor" no cuadra entre servicios.
**Decisión necesaria** (ver final):
- (A) Migrar `Product.sellerId` a UUID (coherente con el resto). Recomendado.
- (B) Dejar `Product.id` como Long (es razonable para catálogo) pero `sellerId` → UUID.
- (C) Dejarlo como está y documentar que sellerId no referencia al usuario real (no recomendado).

### 1.2 🟠 Respuestas de error uniformes
**Problema:** cada servicio devuelve un formato de error distinto (auth muy completo; recommendation casi nada).
El frontend tendría que manejar 3-4 formas distintas.
**Fix:** un `GlobalExceptionHandler` con formato único `{ timestamp, status, error, message, path }` en
todos los servicios (order, payment, messaging, notification, recommendation, review). Incluir 400/401/403/404/409/500.

### 1.3 🟠 Autorización por rol donde falta
**Problema:** order y payment no tienen `@PreAuthorize`; cualquier autenticado puede cambiar estados.
**Fix:** reglas mínimas coherentes con el negocio:
- `PATCH /api/pedidos/{id}/estado` y `.../estado-pago` → internos/ADMIN (atado a la decisión 0.1).
- `POST /api/pedidos` → comprador autenticado.
- `POST /api/pagos` → comprador autenticado.

---

## FASE 2 — Pago real (Mercado Pago)

### 2.1 🟠 Integración de Mercado Pago
**Problema:** payment NO llama a la API de MP. Genera `preferenciaId` aleatorio y una URL de checkout falsa;
el `access-token` configurado nunca se usa. No se puede pagar de verdad.
**Decisión necesaria** (ver final):
- (A) **Integración real sandbox**: crear `MercadoPagoClient` que llame a `/checkout/preferences` con el
  access-token, devolver el `init_point` real y validar la firma del webhook (HMAC). Requiere tus credenciales sandbox de MP.
- (B) **Mock limpio y honesto**: mantener simulado pero determinista y bien documentado (un "proveedor de prueba"),
  con un endpoint para confirmar/rechazar el pago. Suficiente para que el frontend integre el flujo completo.

---

## FASE 3 — Habilitar el frontend (DX)

### 3.1 🟡 Contrato de API + datos semilla
- Documento/colección con todos los endpoints del gateway (rutas en español: `/api/auth`, `/api/productos`,
  `/api/categorias`, `/api/pedidos`, `/api/pagos`, `/api/mensajes`, `/api/notificaciones`, `/api/resenas`, `/api/recomendaciones`).
- Datos semilla (categorías + algunos productos + un SELLER) para que el front tenga con qué trabajar.

### 3.2 🟡 Swagger agregado en el gateway
Un único `/swagger-ui` en el gateway que liste todos los servicios, en vez de 8 URLs separadas.

### 3.3 🟡 Forma de respuesta consistente para listados
Unificar paginación (Page de Spring) y formatos de fecha para que el front no haga casos especiales.

---

## FASE 4 — Higiene de producción (no bloquea el front, pero pediste TODO)

- ⚪ Perfiles Spring `dev`/`prod`: en `prod` → `ddl-auto=validate`, `show-sql=false`, logging `WARN`.
- ⚪ Quitar `logging.level...=DEBUG` (auth, notification) del perfil por defecto.
- ⚪ Secretos solo por variable de entorno (JWT, DB, MP, ADMIN).
- ⚪ (Opcional) Migraciones con Flyway en vez de `ddl-auto=update`.

---

## FASE 5 — Verificación de extremo a extremo

1. `mvnw clean compile` por servicio (o build agregado) — corre en tu máquina (Java 17+).
2. `docker compose up -d` (infra) + levantar eureka → config → servicios → gateway.
3. **Smoke test del flujo completo** (script): register → login → (admin) crear categoría → (seller) crear producto →
   crear pedido → crear pago → confirmar pago → verificar que order pasa a pagado, llega notificación y se puede reseñar.
4. Verificar en Eureka que los 9 servicios están `UP` y que el gateway rutea bien.

---

## Resumen de prioridades

| # | Acción | Prioridad | Desbloquea |
|---|---|---|---|
| 0.1 | Propagar JWT entre servicios (Feign interceptor) | 🔴 | Crear pedido, pagar, reseñar |
| 0.2 | CORS en el gateway | 🔴 | Que el SPA pueda llamar al backend |
| 0.3 | Bootstrap ADMIN | 🔴 | Gestionar categorías / promover sellers |
| 1.1 | Unificar tipo de `sellerId` (UUID) | 🟠 | Relación vendedor↔producto↔pedido |
| 1.2 | Errores uniformes | 🟠 | Manejo de errores simple en el front |
| 1.3 | `@PreAuthorize` en order/payment | 🟠 | Seguridad coherente |
| 2.1 | Mercado Pago (real o mock limpio) | 🟠 | Pago funcional |
| 3.x | Contrato API, seed, Swagger gateway | 🟡 | Velocidad del front |
| 4.x | Perfiles dev/prod, secretos | ⚪ | Producción |
| 5.x | Build + smoke test e2e | 🔴 | Confianza de que todo funciona |

## Frontend recomendado
**React + Vite** (tu config de pagos ya apunta a `localhost:3000`). Un solo cliente HTTP contra el gateway
con interceptor que añade `Authorization: Bearer`. Alternativa: Angular si el curso valora estructura tipo Java.
