# Guía de extras: Monitoreo, GraphQL y Balanceo de carga

Estas tres funciones se añadieron sobre el proyecto existente **sin romper nada**:
son aditivas. Aquí está cómo levantarlas, probarlas y capturarlas para el entregable.

---

## 1. Monitoreo — Prometheus + Grafana

Cada microservicio expone métricas (Micrometer) en `/actuator/prometheus`.
Prometheus las recolecta y Grafana las grafica.

**Levantar** (ya vienen en el compose principal):

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

> El `--build` es necesario la primera vez porque se agregó la dependencia
> `micrometer-registry-prometheus` a los 9 servicios.

**Acceder:**

- Prometheus: http://localhost:9090
  - En *Status → Targets* deberías ver los 9 servicios en estado `UP`.
- Grafana: http://localhost:3001  (usuario `admin`, contraseña `admin`)
  - Dashboard ya provisionado: **"Marketplace · Microservicios"**.
  - Paneles: servicios UP, peticiones HTTP/s, latencia, errores 5xx, memoria JVM e hilos.

**Para la demo:** abre el dashboard, genera tráfico (navega el frontend o corre
`scripts/ataque.py`) y verás los picos en tiempo real. Excelente captura para
sustentación.

### Dashboard extra: "Marketplace · Resiliencia e Infra"

Segundo dashboard ya provisionado, con:
- **Circuit Breakers (Resilience4j)**: estado abierto/cerrado, tasa de fallo y
  llamadas fallidas. Ideal para el entregable de resiliencia.
- **HikariCP**: conexiones activas y en espera por servicio (salud de BD).
- **Kafka**: mensajes producidos/consumidos por segundo (la saga por eventos).
- **Caché**: aciertos vs fallos.

> Algunos paneles solo muestran datos cuando hay actividad (p. ej. los breakers
> se "abren" al provocar fallos con `scripts/ataque.py`, y Kafka cuando se crea
> un pedido/pago).

### Alertas

Reglas ya provisionadas en **Alerting → Alert rules** (carpeta *Alertas Marketplace*):
- **Servicio caído** (`up < 1`) — severidad crítica.
- **Latencia HTTP alta** (> 1 s de media).
- **Errores 5xx elevados**.

Para la demo: mata un servicio (`docker kill ...`) y en ~1 min la regla
"Servicio caído" pasa a **Firing** (rojo). Captura perfecta de monitoreo activo.

### Recargar Grafana tras estos cambios

Los dashboards y alertas se cargan al **arrancar** Grafana, así que reinícialo:

```bash
docker compose -f docker-compose.prod.yml restart grafana
```

(No necesitas reconstruir los microservicios: las métricas de Hikari, Kafka y
Resilience4j ya se exponen automáticamente.)

### Importar dashboards de la comunidad (opcional)

En Grafana: **Dashboards → New → Import** y pega un ID:
- `4701` — JVM (Micrometer)
- `19004` — Spring Boot Statistics

Elige "Prometheus" como datasource al importar.

---

## 2. GraphQL — en product-service (convive con REST)

El catálogo ahora también se puede consultar por GraphQL, además del REST.

**Interfaz interactiva (GraphiQL):**

- http://localhost:8082/graphiql

**Endpoint:** `POST http://localhost:8082/graphql`

**Consultas de ejemplo** (pégalas en GraphiQL):

```graphql
# Lista de productos (elige solo los campos que necesitas)
query {
  products(limit: 5) {
    id
    name
    price
    sellerName
    categoryName
  }
}
```

```graphql
# Un producto puntual
query {
  product(id: 1) {
    name
    description
    price
    stock
  }
}
```

```graphql
# Categorías
query {
  categories { id name }
}
```

**Punto fuerte para la sustentación:** con GraphQL el cliente pide *exactamente*
los campos que quiere en una sola petición, a diferencia del REST que devuelve
el objeto completo. Compáralo: `GET /api/productos` (REST) vs. la query de arriba.

---

## 3. Balanceo de carga — dos capas

El proyecto ya balanceaba **del lado del cliente** (el API Gateway usa Spring
Cloud LoadBalancer sobre Eureka: rutas `lb://`). Ahora añadimos además un
**balanceador Nginx** por delante del gateway, para demostrar balanceo a nivel
de infraestructura.

```
Cliente → Nginx (8090) → [api-gateway ×N] → (Eureka) → [product-service ×N]
            ↑ balanceo infra              ↑ balanceo cliente (ya existía)
```

**Levantar la demo** (usa el override `docker-compose.scale.yml`):

```bash
docker compose -f docker-compose.prod.yml -f docker-compose.scale.yml up -d \
    --scale api-gateway=3 --scale product-service=3
```

> Requiere Docker Compose v2.24+ (por la directiva `!reset`, que quita los
> puertos fijos para poder correr varias réplicas).

**Comprobar el balanceo:**

1. Entra varias veces por el Nginx y mira la cabecera `X-Served-By` (cambia de réplica):

   ```bash
   for i in 1 2 3 4 5 6; do curl -s -D - -o /dev/null http://localhost:8090/api/productos | grep -i x-served-by; done
   ```

2. Mira cómo Eureka registró 3 instancias de cada servicio: http://localhost:8761

3. Logs del Nginx (muestran a qué upstream fue cada request):

   ```bash
   docker compose -f docker-compose.prod.yml -f docker-compose.scale.yml logs -f nginx-lb
   ```

**Demostrar tolerancia a fallos junto al balanceo:** tira una réplica y el resto
sigue atendiendo.

```bash
docker kill $(docker ps --filter "name=product-service" -q | head -1)
# Repite el curl: sigue respondiendo desde las otras réplicas.
```

**Volver al modo normal** (1 réplica, con los puertos de Swagger):

```bash
docker compose -f docker-compose.prod.yml -f docker-compose.scale.yml down
docker compose -f docker-compose.prod.yml up -d
```

---

## 4. Trazas distribuidas — Zipkin

Cada microservicio reporta sus trazas (Micrometer Tracing + Brave) a Zipkin.
Permite seguir UNA petición a través de todos los servicios (gateway → product
→ order → kafka…) y ver cuánto tarda cada salto.

- **UI de Zipkin:** http://localhost:9411

**Para la demo:**
1. Genera tráfico (navega el frontend o llama un endpoint que cruce servicios,
   p. ej. crear un pedido que dispare pago vía Kafka).
2. En Zipkin → *Run Query* → verás las trazas. Abre una para ver el árbol de
   spans entre servicios y dónde se fue el tiempo.
3. Los logs ahora incluyen `traceId`/`spanId`, así puedes correlacionar log ↔ traza.

> Muestreo al 100% (`MANAGEMENT_TRACING_SAMPLING_PROBABILITY=1.0`) para que en la
> demo se capture todo. En producción real se baja (p. ej. 0.1).

---

## 5. Logs centralizados — Loki + Promtail (dentro de Grafana)

Promtail recolecta los logs de **todos** los contenedores y los manda a Loki.
Se consultan desde el mismo Grafana, sin entrar contenedor por contenedor.

**Cómo verlos:**
1. Grafana (http://localhost:3001) → menú **Explore**.
2. Arriba a la izquierda, elige el datasource **Loki**.
3. Consulta por servicio, por ejemplo:
   ```
   {service="auth-service"}
   ```
   o busca errores en todos:
   ```
   {project=~".+"} |= "ERROR"
   ```

> Requiere acceso al socket de Docker (ya montado en `promtail` dentro del
> compose). Funciona en Docker Desktop (WSL2).

---

## 6. Rate limiting en el API Gateway

El gateway limita las peticiones por **IP de cliente** usando un contador en
**Redis** (ventana fija). Al estar en Redis, el límite se comparte entre todas
las réplicas del gateway.

- Por defecto: **100 peticiones cada 60 s** por IP.
- Configurable por entorno: `RATELIMIT_CAPACITY`, `RATELIMIT_WINDOW_SECONDS`,
  `RATELIMIT_ENABLED`.

**Probarlo:** baja el límite para verlo rápido y dispara muchas peticiones.

```bash
# Reinicia el gateway con un límite bajo (5 cada 60 s):
RATELIMIT_CAPACITY=5 docker compose -f docker-compose.prod.yml up -d api-gateway

# Dispara 12 peticiones: las primeras 5 dan 200, el resto 429.
for i in $(seq 1 12); do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost/api/productos
done
```

Verás `200` varias veces y luego `429` (Too Many Requests). La respuesta incluye
cabeceras `X-RateLimit-Limit`, `X-RateLimit-Remaining` y `Retry-After`.

> Si Redis se cae, el filtro deja pasar el tráfico (fail-open) para no tumbar el
> sitio por un problema del limitador.

---

## Puertos nuevos (resumen)

| Servicio | URL |
|---|---|
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 (admin/admin) |
| GraphiQL | http://localhost:8082/graphiql |
| Zipkin (trazas) | http://localhost:9411 |
| Loki (vía Grafana → Explore) | http://localhost:3100 |
| Nginx LB (solo en demo de escala) | http://localhost:8090 |
