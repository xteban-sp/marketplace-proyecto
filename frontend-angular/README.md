# Frontend Angular — Marketplace

Cliente web en Angular 18 que integra al menos dos microservicios REST (`product-service` y `order-service`) a través del **API Gateway** en `http://localhost:8080`.

## Microservicios consumidos

| Microservicio | Ruta Gateway | Método |
|---|---|---|
| product-service | `/api/productos` | GET, GET/:id |
| product-service | `/api/categorias` | GET |
| order-service | `/api/pedidos` | GET, POST |
| review-service | `/api/resenas` | GET, POST |

## Requisitos previos

- Node.js 20+
- Angular CLI 18+: `npm install -g @angular/cli`
- Backend corriendo (ver `docker-compose.yml` en la raíz)

## Instalación y ejecución

```bash
cd frontend-angular
npm install
ng serve
```

Abrir en: `http://localhost:4200`

## Arquitectura de carpetas

```
src/app/
├── core/
│   ├── models/          # Interfaces TypeScript (Product, Order, Review)
│   ├── services/        # Servicios HTTP centralizados
│   └── interceptors/    # HTTP interceptor global de errores
├── features/
│   ├── productos/       # Listado y detalle de productos
│   ├── pedidos/         # Carrito y envío de pedidos
│   └── resenas/         # Listado y envío de reseñas
├── shared/
│   └── components/      # Componentes reutilizables (loader, error)
environments/
├── environment.ts       # URL base del gateway
└── environment.prod.ts
```

## Buenas prácticas aplicadas

- Servicios HTTP centralizados con `HttpClient`
- Modelos tipados con interfaces (no `any`)
- URL base en `environment.ts`
- Manejo de errores con `{ next, error }` en cada suscripción
- Interceptor global para logging de errores HTTP
- Componentes separados por feature (arquitectura limpia)
- CORS configurado en el Gateway para `localhost:4200`
