# Demostración de Resiliencia

Cómo demostrar ante el profesor: (1) ataques/carga con Python, (2) Circuit Breaker
(Resilience4j) que degrada con gracia, y (3) autocuración + failover ("si se cae un
microservicio, se levanta/responde otro").

**Requisitos:** el stack corriendo (`docker compose -f docker-compose.prod.yml up -d`) y
Python instalado. Los scripts están en `scripts/` y solo usan la librería estándar
(no necesitan `pip install`). En Windows, si `python` no funciona, usa `py`.

---

## Demo 1 — Ataque de carga (el sistema aguanta)

Bombardea el catálogo con muchas peticiones concurrentes:

```bash
python scripts/ataque.py --target catalogo --hilos 80 --segundos 20
```
Al final muestra peticiones totales, OK, fallidas, **requests/segundo** y latencia media.
Mensaje clave: el sistema sigue respondiendo bajo carga (la mayoría 2xx).

También puedes atacar el login:
```bash
python scripts/ataque.py --target login --hilos 50 --segundos 15
```

## Demo 2 — Circuit Breaker (Resilience4j degrada con gracia)

Hay un endpoint que **siempre falla** en el servidor a propósito
(`/api/pagos/test-resilience`): llama a una dependencia que lanza error. En vez de
romperse, Resilience4j hace **retry → fallback** y responde "DEGRADED".

```bash
python scripts/ataque.py --target breaker --hilos 30 --segundos 15
```
Verás muchas **"respuestas degradadas (fallback)"** y código HTTP 200: el servicio
**no se cayó** aunque su dependencia fallaba en cada llamada. Eso es el Circuit Breaker.

> Detalle técnico: tras varios fallos seguidos, el circuito se "abre" y las llamadas
> van directo al fallback (más rápido, sin reintentar), protegiendo al sistema.

## Demo 3 — Autocuración (si se cae, se levanta solo)

El `docker-compose.prod.yml` usa `restart: unless-stopped`: si un contenedor muere,
Docker lo vuelve a levantar automáticamente.

1. En una terminal, deja el monitor vigilando el catálogo:
   ```bash
   python scripts/monitor.py --path "/api/productos?page=0&size=1"
   ```
2. En **otra** terminal, mata el microservicio de productos:
   ```bash
   docker kill marketplace-proyecto-product-service-1
   ```
3. Observa el monitor: empieza a marcar **CAÍDO** unos segundos y luego **se recupera solo**
   cuando Docker reinicia el contenedor. Confírmalo con:
   ```bash
   docker compose -f docker-compose.prod.yml ps
   ```
   (verás product-service como `Restarting` y luego `Up` de nuevo).

## Demo 4 — Failover con réplicas (otro responde sin caída)

Aquí mostramos que con **2 instancias** del servicio, si matas una, la otra sigue
respondiendo gracias al balanceo del gateway vía Eureka (`lb://`).

1. Escala productos a 2 réplicas:
   ```bash
   docker compose -f docker-compose.prod.yml up -d --scale product-service=2
   ```
   (espera ~30 s a que la segunda se registre en Eureka: `http://localhost:8761` no está
   publicado, pero puedes confirmar con `docker compose ... ps` que hay 2 product-service).
2. Deja el monitor corriendo:
   ```bash
   python scripts/monitor.py --path "/api/productos?page=0&size=1"
   ```
3. Mata **una** de las dos réplicas:
   ```bash
   docker kill marketplace-proyecto-product-service-1
   ```
4. El monitor sigue marcando **OK** (con quizá 1–2 fallos transitorios mientras Eureka
   saca de la lista a la instancia muerta): la otra réplica atiende las peticiones.
   → "Si se cae un microservicio, otro responde".

Para volver a una sola instancia:
```bash
docker compose -f docker-compose.prod.yml up -d --scale product-service=1
```

---

## Qué decir en la sustentación (resumen)

- **Resiliencia ante fallos de dependencias:** Resilience4j con Circuit Breaker + Retry +
  Fallback (Demo 2). El servicio degrada en vez de caer.
- **Autocuración:** política `restart: unless-stopped` en Docker; un contenedor caído se
  reinicia solo (Demo 3).
- **Alta disponibilidad / failover:** múltiples réplicas registradas en Eureka y balanceadas
  por el API Gateway; si una cae, otra atiende (Demo 4).
- **Tolerancia a carga:** prueba de estrés con Python (Demo 1).
