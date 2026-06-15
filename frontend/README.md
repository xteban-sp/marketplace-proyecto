# Feria · Frontend (React + Vite)

SPA del marketplace universitario. Habla **solo con el API Gateway** (`http://localhost:8080`)
y guarda el JWT del login para mandarlo en cada petición.

## Requisitos
- Node.js 18+ (incluye npm).
- El backend corriendo (gateway en :8080, auth y product arriba). Ver `../GUIA_ARRANQUE.md`.

## Correr en desarrollo
```bash
cd frontend
npm install
npm run dev
```
Abre http://localhost:3000

## Configuración
El `.env` tiene:
```
VITE_API_URL=http://localhost:8080
VITE_CLOUDINARY_CLOUD_NAME=
VITE_CLOUDINARY_UPLOAD_PRESET=
```
- `VITE_API_URL`: la dirección del gateway.
- Cloudinary (subida de fotos): crea una cuenta gratis en https://cloudinary.com,
  haz un **Upload preset** en modo **Unsigned** (Settings → Upload), y pega el
  cloud name + el nombre del preset. Si lo dejas vacío, la subida de fotos se
  desactiva y puedes usar igual una URL de imagen.

## Qué incluye esta versión
- **Login y registro** contra `/api/auth`.
- **Catálogo**: listado paginado y búsqueda por nombre/categoría (`/api/productos`, `/api/categorias`).
- **Detalle de producto** (`/api/productos/{id}`).

> Nota: `product-service` exige JWT, así que el catálogo se ve **después** de iniciar sesión.
> Para entrar rápido puedes usar el admin que crea el backend: usuario `admin`, contraseña `Admin12345!`.

## Siguiente etapa (pendiente)
Carrito, creación de pedido y pago con Mercado Pago, mensajes, reseñas y panel de vendedor.

## Estructura
```
src/
  api/client.js        Axios + interceptor que añade el token y maneja 401
  auth/AuthContext.jsx Estado de sesión (login/registro/logout)
  components/          Navbar, ProductCard
  pages/               Login, Register, Catalog, ProductDetail
  index.css            Sistema de diseño (paleta + tipografía propias)
```
