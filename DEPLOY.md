# Desplegar en internet (gratis, 24/7) — Oracle Cloud + Docker + Caddy

Esta guía levanta **todo** el marketplace (microservicios + bases de datos + Kafka +
frontend) en una sola VM gratuita, accesible por un dominio con HTTPS.

Arquitectura en la nube: una VM corre `docker-compose.prod.yml`. **Caddy** (servicio `web`)
sirve el frontend y reenvía `/api/*` al gateway, todo bajo tu dominio y con HTTPS automático.
Como frontend y API comparten dominio, no hay problemas de CORS ni de "mixed content".

> Aviso honesto: es un despliegue real y la primera vez algo puede fallar (config, puertos,
> memoria). Sigue los pasos; si algo truena, copia el log del contenedor que falle y lo arreglamos.

---

## 1. Crear la VM (Oracle Cloud Always Free)

1. Crea cuenta en https://www.oracle.com/cloud/free/ (pide tarjeta para verificar, no cobra en el free tier).
2. **Create Instance** → imagen **Ubuntu 22.04** → shape **VM.Standard.A1.Flex** (ARM):
   pon **4 OCPU y 24 GB RAM** (todo el free tier en una VM).
   - Si sale "Out of capacity", reintenta más tarde o cambia de Availability Domain. Es común.
3. Descarga la **clave SSH** que te genera.
4. Cuando esté lista, anota su **IP pública**.

> Alternativa de pago si Oracle te complica: un VPS de Hetzner/DigitalOcean (~5 USD/mes). Los pasos 4 en adelante son iguales.

## 2. Abrir puertos 80 y 443

Oracle bloquea puertos por **dos** sitios; abre ambos:

1. **Security List** (en la consola de Oracle): VCN → Subnet → Security List → Add Ingress Rules:
   - Source `0.0.0.0/0`, TCP, puerto **80**
   - Source `0.0.0.0/0`, TCP, puerto **443**
2. **Firewall dentro de la VM** (por SSH):
   ```bash
   sudo iptables -I INPUT -p tcp --dport 80 -j ACCEPT
   sudo iptables -I INPUT -p tcp --dport 443 -j ACCEPT
   sudo netfilter-persistent save
   ```

## 3. Dominio gratis (DuckDNS) apuntando a tu VM

1. Entra a https://www.duckdns.org con tu cuenta Google/GitHub.
2. Crea un subdominio (ej. `mimarketplace`) → te queda `mimarketplace.duckdns.org`.
3. En el campo **current ip** pon la **IP pública** de tu VM y guarda.

## 4. Instalar Docker en la VM

Por SSH (`ssh -i tuclave ubuntu@IP_PUBLICA`):
```bash
sudo apt update && sudo apt install -y docker.io docker-compose-plugin git
sudo usermod -aG docker $USER
# cierra sesión y vuelve a entrar para aplicar el grupo
```

## 5. Traer el proyecto y configurar

```bash
git clone <URL_DE_TU_REPO> marketplace
cd marketplace
cp .env.deploy.example .env
nano .env   # completa SITE_ADDRESS, PUBLIC_URL, JWT_SECRET, ADMIN_PASSWORD, etc.
```
- `SITE_ADDRESS` = tu dominio DuckDNS **sin** https (ej. `mimarketplace.duckdns.org`).
- `PUBLIC_URL` = la URL completa con https (ej. `https://mimarketplace.duckdns.org`).

## 6. Levantar todo

```bash
docker compose -f docker-compose.prod.yml up -d --build
```
La primera vez **tarda bastante** (compila 9 servicios Java y el frontend). Paciencia.

Ver el progreso / estado:
```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f api-gateway
```

## 7. Probar

Abre `https://tudominio.duckdns.org` — Caddy ya habrá sacado el certificado HTTPS.
Entra con el admin (`ADMIN_USERNAME` / `ADMIN_PASSWORD`) y verás el catálogo.

---

## Orden de arranque y errores típicos

- Los microservicios reintentan conexión a Eureka/Config/BD, así que el orden se acomoda solo.
  Si uno queda reiniciando, mira su log: casi siempre es la BD aún no lista (esperar) o una variable mal puesta.
- **HTTPS no sale:** revisa que el dominio DuckDNS apunte a la IP correcta y que los puertos 80/443 estén abiertos (paso 2). Caddy necesita el 80 para el certificado.
- **Memoria:** 24 GB alcanzan para todo. Si usas una VM más chica, comenta en el compose los servicios que no demuestres (messaging, recommendation, etc.).
- **Actualizar tras cambios:** `git pull` y de nuevo `docker compose -f docker-compose.prod.yml up -d --build`.

## Variables (en el archivo .env)

| Variable | Para qué |
|---|---|
| `SITE_ADDRESS` | Tu dominio (sin https). Caddy lo usa para el certificado. **Sin esta var, corre en http://localhost** |
| `PUBLIC_URL` | URL pública completa (https://dominio). Usada en correos, pagos y CORS |
| `JWT_SECRET` | Secret JWT compartido (mín. 32 chars) |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | Admin inicial |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | Verificación por correo (opcional) |
| `MERCADOPAGO_ACCESS_TOKEN` | Pago real sandbox (opcional) |

---

## Correr TODO en local con Docker (sin VM, para probar la dockerización)

En tu PC con Docker Desktop encendido, desde la raíz del proyecto:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```
- **No necesitas `.env` ni dominio**: por defecto el frontend queda en **http://localhost** y el API en **http://localhost/api**.
- La primera vez tarda (compila los 9 servicios Java + el frontend).
- Ver estado: `docker compose -f docker-compose.prod.yml ps`
- Ver logs de uno: `docker compose -f docker-compose.prod.yml logs -f api-gateway`
- Apagar todo: `docker compose -f docker-compose.prod.yml down`

Esto levanta **toda la plataforma dockerizada con un solo comando** — ideal para mostrar al profesor que el proyecto está contenedorizado.
