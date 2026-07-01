# Guía de Keycloak (Identity Provider)

> **Importante:** Keycloak se añadió de forma **opcional y aislada**. El sistema
> de autenticación actual (JWT propio en `auth-service`) **sigue siendo el que usa
> la app** y funciona igual. Keycloak se levanta aparte para demostrarlo y dejar
> lista una ruta de integración. Así no se arriesga lo que ya funciona.

---

## 1. Levantar Keycloak

```bash
docker compose -f docker-compose.prod.yml -f docker-compose.keycloak.yml up -d keycloak
```

La primera vez tarda un poco (descarga la imagen e importa el realm).

- **Consola de administración:** http://localhost:8180
- **Usuario admin de la consola:** `admin` / `admin`

---

## 2. Qué viene preconfigurado (realm `marketplace`)

Al importarse `keycloak/realm-marketplace.json` se crea:

**Roles** (de realm): `USER`, `SELLER`, `ADMIN` — los mismos del proyecto.

**Clientes:**
- `marketplace-frontend` — cliente *público* (SPA React), con flujo estándar
  (OIDC/Authorization Code) y los `redirectUris` de localhost ya permitidos.
- `marketplace-api` — cliente *bearer-only* para los resource servers (gateway
  y microservicios), secreto `marketplace-api-secret`.

**Usuarios de demo** (para capturas y pruebas):

| Usuario | Contraseña | Roles |
|---|---|---|
| `admin-kc` | `Admin12345!` | ADMIN, USER |
| `vendedor-kc` | `Password1!` | SELLER, USER |
| `comprador-kc` | `Password1!` | USER |

---

## 3. Probar que emite tokens (para la sustentación)

Pide un token por *Direct Access Grant* (solo para demo):

```bash
curl -s -X POST \
  http://localhost:8180/realms/marketplace/protocol/openid-connect/token \
  -d "client_id=marketplace-frontend" \
  -d "grant_type=password" \
  -d "username=admin-kc" \
  -d "password=Admin12345!"
```

Te devuelve un `access_token` (JWT). Pégalo en https://jwt.io y muestra los
claims (`realm_access.roles` con ADMIN/USER, `preferred_username`, etc.).

Endpoints útiles para capturar:
- Configuración OIDC: `http://localhost:8180/realms/marketplace/.well-known/openid-configuration`
- Claves públicas (JWKS): `http://localhost:8180/realms/marketplace/protocol/openid-connect/certs`

---

## 4. Ruta de integración (si decides migrar) — OPCIONAL

Hoy tus servicios validan un JWT **HS256** con un secreto compartido. Keycloak
emite JWT **RS256** firmados con clave privada; los servicios los validarían
contra el **JWKS** público. La migración sería:

1. Añadir a cada microservicio el starter de resource server:
   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
   </dependency>
   ```
2. Configurar el issuer (en lugar del filtro JWT propio):
   ```yaml
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: http://keycloak:8080/realms/marketplace
   ```
3. Mapear `realm_access.roles` → authorities `ROLE_*` con un converter.
4. En el frontend, reemplazar el login propio por OIDC (p. ej. `keycloak-js`)
   apuntando al cliente `marketplace-frontend`.

> Recomendación: hacer esto **después** de tener todo lo demás verificado y
> capturado, y en una rama aparte. Es un cambio transversal a los 9 servicios.

---

## 5. Apagar solo Keycloak

```bash
docker compose -f docker-compose.prod.yml -f docker-compose.keycloak.yml stop keycloak
```
