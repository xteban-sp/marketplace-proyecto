#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Genera ENTREGABLE_PROYECTO.docx (OOXML puro, sin dependencias)."""
import zipfile, html, os

OUT = os.path.join(os.path.dirname(__file__), "..", "ENTREGABLE_PROYECTO.docx")
body = []

def esc(t): return html.escape(str(t), quote=False)
def H1(t): body.append(f'<w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t xml:space="preserve">{esc(t)}</w:t></w:r></w:p>')
def H2(t): body.append(f'<w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t xml:space="preserve">{esc(t)}</w:t></w:r></w:p>')
def H3(t): body.append(f'<w:p><w:pPr><w:pStyle w:val="Heading3"/></w:pPr><w:r><w:t xml:space="preserve">{esc(t)}</w:t></w:r></w:p>')
def P(t): body.append(f'<w:p><w:pPr><w:spacing w:after="120"/></w:pPr><w:r><w:t xml:space="preserve">{esc(t)}</w:t></w:r></w:p>')
def Pb(label, t):
    body.append(f'<w:p><w:pPr><w:spacing w:after="120"/></w:pPr>'
                f'<w:r><w:rPr><w:b/></w:rPr><w:t xml:space="preserve">{esc(label)}: </w:t></w:r>'
                f'<w:r><w:t xml:space="preserve">{esc(t)}</w:t></w:r></w:p>')
def B(t):
    body.append(f'<w:p><w:pPr><w:ind w:left="600" w:hanging="260"/><w:spacing w:after="40"/></w:pPr>'
                f'<w:r><w:t xml:space="preserve">•  {esc(t)}</w:t></w:r></w:p>')
def CODE(t):
    for line in t.split("\n"):
        line = line if line.strip() else " "
        body.append('<w:p><w:pPr><w:pStyle w:val="Code"/></w:pPr>'
                    f'<w:r><w:t xml:space="preserve">{esc(line)}</w:t></w:r></w:p>')
def SHOT(label):
    bd = '<w:pBdr>' + ''.join(f'<w:{s} w:val="dashed" w:sz="6" w:space="6" w:color="6366F1"/>' for s in ("top","left","bottom","right")) + '</w:pBdr>'
    body.append(f'<w:p><w:pPr>{bd}<w:jc w:val="center"/><w:spacing w:before="120" w:after="200"/></w:pPr>'
                f'<w:r><w:rPr><w:i/><w:color w:val="6366F1"/></w:rPr>'
                f'<w:t xml:space="preserve">[ INSERTAR CAPTURA: {esc(label)} ]</w:t></w:r></w:p>')
def PBREAK(): body.append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')
def TITLE(t): body.append(f'<w:p><w:pPr><w:pStyle w:val="Title"/></w:pPr><w:r><w:t xml:space="preserve">{esc(t)}</w:t></w:r></w:p>')
def SUB(t): body.append(f'<w:p><w:pPr><w:spacing w:after="120"/></w:pPr><w:r><w:rPr><w:color w:val="8A8D99"/><w:sz w:val="26"/></w:rPr><w:t xml:space="preserve">{esc(t)}</w:t></w:r></w:p>')

# ===================== PORTADA =====================
TITLE("Marketplace Universitario “Feria”")
SUB("Arquitectura de Microservicios con Modelo C4 — Documento de Entregables")
P("Curso: Desarrollo de Aplicaciones Distribuidas (DAD)")
P("Integrante(s): _________________________   Docente: _________________________")
P("Fecha: ____ / ____ / 2026")
SHOT("Portada / logo del equipo (opcional)")
PBREAK()

# Resumen del stack (referencia transversal)
H1("Resumen técnico del sistema")
P("Feria es un marketplace universitario construido con una arquitectura de microservicios. "
  "Permite a estudiantes registrarse, publicar productos, navegar el catálogo, comprar, pagar, "
  "recibir notificaciones, dejar reseñas y comunicarse entre comprador y vendedor.")
Pb("Lenguaje/Framework", "Java 21, Spring Boot 3.5.13, Spring Cloud 2025.0.2")
Pb("Plataforma de microservicios", "Eureka (descubrimiento), Spring Cloud Config (configuración), Spring Cloud Gateway (puerta de enlace)")
Pb("Resiliencia", "Resilience4j (Circuit Breaker, Retry, Fallback) + balanceo por Eureka/Spring Cloud LoadBalancer")
Pb("Mensajería/Eventos", "Apache Kafka (comunicación asíncrona entre servicios)")
Pb("Persistencia", "PostgreSQL 16 (6 servicios) y MySQL 8.4 (2 servicios) — persistencia políglota; Redis para caché")
Pb("Seguridad", "JWT (jjwt 0.12.6) con roles USER, SELLER, ADMIN y token de servicio SERVICE")
Pb("Frontend", "React 18 + Vite, servido con Caddy (HTTPS automático)")
Pb("Integraciones", "Mercado Pago (pasarela de pago, sandbox) y Cloudinary (imágenes)")
Pb("Despliegue", "Docker + Docker Compose (todo contenedorizado, un solo comando)")
H2("Servicios y puertos")
for s in [
  "eureka-server : 8761 — servidor de descubrimiento",
  "config-service : 8888 — configuración centralizada",
  "api-gateway : 8080 — puerta de enlace única",
  "auth-service : 8081 — PostgreSQL — registro, login, JWT, roles",
  "product-service : 8082 — MySQL + Redis — productos y categorías",
  "order-service : 8083 — PostgreSQL + Kafka — pedidos",
  "payment-service : 8084 — MySQL + Kafka — pagos (Mercado Pago)",
  "messaging-service : 8085 — PostgreSQL — mensajería comprador-vendedor",
  "notification-service : 8086 — PostgreSQL + Kafka — notificaciones",
  "recommendation-service : 8087 — PostgreSQL + Redis + Kafka — recomendaciones",
  "review-service : 8088 — PostgreSQL — reseñas",
]:
    B(s)
PBREAK()

# ===================== ENTREGABLE 01 =====================
H1("Entregable 01. Documento de Análisis y Diseño")
H2("Descripción del problema")
P("En el campus universitario, los estudiantes compran y venden productos (libros, electrónica, ropa, "
  "servicios) por canales informales (grupos de chat, anuncios físicos) que no ofrecen búsqueda, "
  "confianza, pagos seguros ni historial. Hace falta una plataforma centralizada y segura.")
H2("Justificación")
P("Una arquitectura de microservicios permite escalar e implementar de forma independiente cada "
  "capacidad (catálogo, pagos, mensajería), tolerar fallos parciales y mantener el sistema disponible. "
  "Es además el enfoque que exige el curso de Aplicaciones Distribuidas.")
H2("Objetivos")
B("Objetivo general: desarrollar un marketplace universitario distribuido, seguro y resiliente.")
B("Construir microservicios independientes con persistencia políglota.")
B("Implementar seguridad con JWT y control de acceso por roles.")
B("Garantizar resiliencia (circuit breaker, reintentos, fallback) y alta disponibilidad.")
B("Ofrecer un frontend usable integrado vía API Gateway.")
H2("Alcance")
P("Incluye: autenticación/roles, catálogo de productos, pedidos, pagos (sandbox), notificaciones, "
  "reseñas, mensajería y recomendaciones. Excluye: facturación electrónica, logística de envíos y "
  "pagos con dinero real en producción.")
H2("Requerimientos funcionales")
for r in ["RF01 Registro e inicio de sesión con JWT.",
          "RF02 Gestión de roles (comprador, vendedor, administrador).",
          "RF03 CRUD de productos y categorías.",
          "RF04 Búsqueda y filtrado del catálogo.",
          "RF05 Creación y seguimiento de pedidos.",
          "RF06 Pago de pedidos mediante pasarela.",
          "RF07 Notificaciones automáticas por eventos (pedido, pago).",
          "RF08 Reseñas validando compra pagada.",
          "RF09 Mensajería comprador-vendedor.",
          "RF10 Recomendaciones por usuario."]:
    B(r)
H2("Requerimientos no funcionales")
for r in ["RNF01 Seguridad: JWT, contraseñas con BCrypt, secretos por variables de entorno.",
          "RNF02 Resiliencia: tolerancia a fallos con Resilience4j y auto-reinicio de contenedores.",
          "RNF03 Disponibilidad: balanceo de carga y múltiples instancias.",
          "RNF04 Escalabilidad: servicios desplegables de forma independiente.",
          "RNF05 Mantenibilidad: configuración centralizada y manejo uniforme de errores.",
          "RNF06 Usabilidad: frontend responsivo con modo claro/oscuro."]:
    B(r)
H2("Casos de uso (resumen)")
B("CU01 Iniciar sesión / registrarse.")
B("CU02 Publicar producto (vendedor).")
B("CU03 Buscar y ver productos.")
B("CU04 Comprar y pagar.")
B("CU05 Recibir notificaciones / reseñar / mensajear.")
SHOT("Diagrama de casos de uso (UML)")
H2("Historias de usuario (ejemplos)")
B("Como comprador, quiero buscar productos por categoría para encontrar lo que necesito.")
B("Como vendedor, quiero publicar un producto con foto para ofrecerlo al campus.")
B("Como comprador, quiero pagar de forma segura para completar mi compra.")
B("Como usuario, quiero recibir una notificación cuando mi pago sea aprobado.")
PBREAK()

# ===================== ENTREGABLE 02 =====================
H1("Entregable 02. Diagramas C4")
P("Se documenta la arquitectura con el modelo C4 en sus cuatro niveles. A continuación la descripción "
  "de cada nivel; inserta el diagrama correspondiente (elaborado en draw.io / Structurizr) en cada marcador.")
H2("Nivel 1 — Context Diagram")
P("Muestra el sistema “Feria” como una caja y sus actores externos: Estudiante (comprador), "
  "Vendedor, Administrador, y los sistemas externos Mercado Pago (pagos), Cloudinary (imágenes) y el "
  "proveedor de correo (verificación de cuenta).")
SHOT("C4 Nivel 1 - Diagrama de Contexto")
H2("Nivel 2 — Container Diagram")
P("Muestra los contenedores: el SPA React (Caddy), el API Gateway, Eureka, Config Server, los 8 "
  "microservicios de dominio, los motores de base de datos (PostgreSQL x6, MySQL x2), Kafka, Zookeeper "
  "y Redis. Las flechas indican el protocolo (HTTPS, REST/JSON, JDBC, Kafka).")
SHOT("C4 Nivel 2 - Diagrama de Contenedores")
H2("Nivel 3 — Component Diagram")
P("Detalla los componentes internos de un microservicio representativo (p. ej. auth-service): "
  "Controller (AuthController), Service (CustomUserDetailsService), filtro JWT (JwtAuthenticationFilter), "
  "utilidad JwtUtil, repositorio (UserRepository) y configuración de seguridad (SecurityConfig).")
SHOT("C4 Nivel 3 - Diagrama de Componentes (auth-service)")
H2("Nivel 4 — Code Diagram")
P("Diagrama de clases del componente elegido. Por ejemplo, la entidad User implementa UserDetails, "
  "con su Set<String> de roles, y su relación con UserRepository y JwtUtil.")
SHOT("C4 Nivel 4 - Diagrama de Clases")
PBREAK()

# ===================== ENTREGABLE 03 =====================
H1("Entregable 03. Repositorio Git")
Pb("Repositorio", "GitHub — https://github.com/xteban-sp/marketplace-proyecto")
P("El repositorio contiene un módulo por microservicio, la carpeta config-repo (configuración "
  "centralizada), el frontend, el docker-compose y la documentación.")
H2("Estructura del repositorio")
CODE("""marketplace-proyecto/
├── eureka-server/        ├── messaging-service/
├── config-service/       ├── notification-service/
├── api-gateway/          ├── recommendation-service/
├── auth-service/         ├── review-service/
├── product-service/      ├── config-repo/        (configuración central)
├── order-service/        ├── frontend/           (React + Vite)
├── payment-service/      ├── scripts/            (pruebas y siembra)
├── docker-compose.yml    └── docker-compose.prod.yml""")
H2("Ramas y commits")
P("El proyecto se desarrolló con ramas por funcionalidad (p. ej. product-branch) integradas a main "
  "mediante merges/pull requests, con commits versionados (v1.1 … V6.0).")
SHOT("Historial de commits (git log) y ramas en GitHub")
SHOT("Lista de colaboradores / contribuciones del equipo")
H2("Instrucciones de ejecución")
P("Documentadas en GUIA_ARRANQUE.md y DEPLOY.md. Resumen para correr todo dockerizado:")
CODE("docker compose -f docker-compose.prod.yml up -d --build\n# Frontend: http://localhost   |   Admin: admin / Admin12345!")
PBREAK()

# ===================== ENTREGABLE 04 =====================
H1("Entregable 04. Microservicios Implementados")
P("Se implementaron 8 microservicios de dominio + 3 de plataforma (Eureka, Config, Gateway). "
  "Cada uno es un proyecto Spring Boot independiente con su propia base de datos.")
H2("Auth Service")
P("Registro, login, validación de token, gestión de roles y bootstrap de administrador. "
  "Las contraseñas se cifran con BCrypt y el rol por defecto al registrarse es USER.")
CODE("""@PostMapping("/register")
@Transactional
public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
    // ... validación de DNI, email, código y celular únicos ...
    Set<String> roles = Set.of("USER");          // el cliente NO elige rol
    User user = User.builder()
        .username(req.getUsername())
        .password(passwordEncoder.encode(req.getPassword()))
        .roles(roles).enabled(true).build();
    userRepository.save(user);
    return ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(user), ...));
}""")
H2("Product Service (CRUD completo)")
P("CRUD de productos y categorías, búsqueda paginada, caché con Redis y autorización por rol "
  "(crear/editar/eliminar requiere SELLER o ADMIN; el vendedor se toma del token JWT).")
CODE("""@PostMapping
@PreAuthorize("hasAnyRole('SELLER','ADMIN')")
public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO dto,
                                                 Authentication auth) {
    dto.setSellerId(UUID.fromString(auth.getName())); // vendedor = usuario del token
    return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
}""")
H2("Order Service")
P("Crea pedidos validando stock contra product-service (vía Feign), calcula totales y emite el "
  "evento Kafka “pedido-creado”. Cambios de estado restringidos a ADMIN/SERVICE.")
H2("Payment Service")
P("Crea el pago, genera la preferencia de Mercado Pago (sandbox) y, al aprobarse, actualiza el "
  "pedido y emite “pago-aprobado” / “pago-fallido”.")
P("Otros microservicios: Messaging (conversaciones y mensajes), Notification (consume eventos Kafka), "
  "Review (reseñas validando compra pagada) y Recommendation (recomendaciones por usuario).")
SHOT("Estructura de paquetes de un microservicio en el IDE")
SHOT("Prueba de un CRUD funcionando (Swagger o Postman)")
PBREAK()

# ===================== ENTREGABLE 05 =====================
H1("Entregable 05. API REST Documentada")
P("Cada microservicio expone su documentación OpenAPI/Swagger (springdoc). Las rutas se consumen "
  "siempre a través del API Gateway (puerto 8080).")
H2("Swagger / OpenAPI")
P("Disponible por servicio en /swagger-ui.html (p. ej. http://localhost:8082/swagger-ui.html para product).")
SHOT("Swagger UI mostrando los endpoints de un servicio")
H2("Endpoints principales (vía Gateway)")
CODE("""POST  /api/auth/register            POST  /api/auth/login
GET   /api/auth/validate            POST  /api/auth/become-seller
GET   /api/productos  (paginado, ?sort=, ?page=, ?size=)
GET   /api/productos/search?name=&categoryId=
POST  /api/productos   PUT /api/productos/{id}   DELETE /api/productos/{id}
GET   /api/categorias               POST /api/categorias  (ADMIN)
POST  /api/pedidos                  GET  /api/pedidos?compradorId=
POST  /api/pagos                    POST /api/pagos/webhook/mercadopago
GET   /api/notificaciones?usuarioId=   PATCH /api/notificaciones/{id}/read
GET   /api/resenas?productId=       POST /api/resenas
POST  /api/mensajes/conversations   POST /api/mensajes
GET   /api/recomendaciones?usuarioId=""")
H2("Ejemplo Request / Response")
CODE("""POST /api/auth/login
{ "username": "admin", "password": "Admin12345!" }

200 OK
{ "token": "eyJhbGciOi...", "roles": ["ROLE_ADMIN","ROLE_USER"], "username": "admin" }""")
H2("Códigos HTTP y formato de error uniforme")
P("Todos los servicios devuelven errores con el mismo formato: "
  "{ timestamp, status, error, message, path, [fieldErrors] }. "
  "Se manejan 200/201, 400 (validación), 401 (no autenticado), 403 (sin permiso), "
  "404 (no encontrado), 409 (conflicto) y 503 (degradado por resiliencia).")
SHOT("Respuesta de la API (200 y un error 4xx) en Swagger/Postman")
PBREAK()

# ===================== ENTREGABLE 06 =====================
H1("Entregable 06. Configuración Centralizada")
P("Se usa Spring Cloud Config Server en modo nativo, sirviendo la carpeta config-repo. Todos los "
  "servicios la importan al arrancar; el secret JWT y otros parámetros se centralizan y se leen por "
  "variable de entorno.")
H2("Config Server")
CODE("""@EnableConfigServer
@SpringBootApplication
public class ConfigServiceApplication { ... }

# application.properties del config-service
spring.cloud.config.server.native.search-locations=file:./config-repo""")
H2("Repositorio de configuración (config-repo)")
P("Contiene application.properties (común a todos) y un archivo por servicio. Ejemplo del valor "
  "compartido del secret JWT, leído por todos los microservicios:")
CODE("jwt.secret=${JWT_SECRET:clave_secreta_dev_marketplace_2026_minimo_32_chars}\njwt.expiration=${JWT_EXPIRATION:86400000}")
SHOT("Carpeta config-repo y un servicio leyendo su configuración en el log de arranque")
PBREAK()

# ===================== ENTREGABLE 07 =====================
H1("Entregable 07. Registro y Descubrimiento (Eureka)")
P("Eureka Server centraliza el registro de servicios. Cada microservicio se registra como cliente y el "
  "Gateway los descubre por nombre lógico (lb://servicio) para enrutar y balancear.")
CODE("""@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication { ... }

# cliente (en cada microservicio)
eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka""")
SHOT("Dashboard de Eureka (http://localhost:8761) con los 9 servicios UP")
PBREAK()

# ===================== ENTREGABLE 08 =====================
H1("Entregable 08. API Gateway")
P("Spring Cloud Gateway es el único punto de entrada. Define rutas a cada servicio, balancea por "
  "Eureka, y centraliza CORS para el frontend.")
H2("Rutas")
CODE("""spring.cloud.gateway.mvc.routes[0].id=auth-service
spring.cloud.gateway.mvc.routes[0].uri=lb://auth-service
spring.cloud.gateway.mvc.routes[0].predicates[0]=Path=/api/auth/**
# ... product (/api/productos, /api/categorias), order (/api/pedidos),
#     payment (/api/pagos), messaging, notification, review, recommendation ...""")
H2("CORS y seguridad")
P("El Gateway concentra la política CORS (orígenes del SPA) para que el frontend y la API compartan "
  "dominio sin conflictos. En el despliegue, Caddy enruta /api/* al Gateway, evitando CORS y "
  "mixed-content. Cada microservicio valida además su propio JWT (seguridad en profundidad).")
CODE("""config.setAllowedOrigins(origenesDelFrontend);
config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
config.setAllowCredentials(true);""")
SHOT("Petición del frontend pasando por el Gateway (DevTools - Network)")
PBREAK()

# ===================== ENTREGABLE 09 =====================
H1("Entregable 09. Seguridad JWT")
P("La autenticación se basa en JSON Web Tokens. auth-service firma el token (HS256) con el secret "
  "compartido; los demás servicios lo validan con la misma clave. Las contraseñas se cifran con BCrypt.")
H2("Login y generación del token")
CODE("""return Jwts.builder()
    .setSubject(user.getUsername())
    .claim("userId", user.getId().toString())
    .claim("roles", new ArrayList<>(user.getRoles()))
    .setExpiration(new Date(System.currentTimeMillis() + expiration))
    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
    .compact();""")
H2("Roles y permisos")
P("Roles del sistema: USER (comprador, por defecto), SELLER (vendedor), ADMIN (administrador, creado "
  "por bootstrap) y SERVICE (token interno entre microservicios). La autorización por método usa "
  "@PreAuthorize.")
CODE("""@PatchMapping("/users/{username}/seller")
@PreAuthorize("hasRole('ADMIN')")    // solo ADMIN promueve a vendedor

@PostMapping  // crear producto
@PreAuthorize("hasAnyRole('SELLER','ADMIN')")""")
H2("Validación de token (filtro en cada servicio)")
CODE("""Claims claims = jwtService.parseClaims(token);
String userId = claims.get("userId", String.class);
var authorities = rolesFrom(claims);   // ROLE_USER, ROLE_SELLER, ...
SecurityContextHolder.getContext().setAuthentication(
    new UsernamePasswordAuthenticationToken(userId, null, authorities));""")
SHOT("Login devolviendo el JWT y una petición protegida con el token (Postman/DevTools)")
PBREAK()

# ===================== ENTREGABLE 10 =====================
H1("Entregable 10. Resiliencia")
P("Se usa Resilience4j para tolerar fallos de dependencias. Los servicios aplican Circuit Breaker + "
  "Retry + Fallback en las llamadas que pueden fallar, devolviendo una respuesta degradada en vez de caer.")
CODE("""@CircuitBreaker(name = "pedidoService", fallbackMethod = "fallbackCrearPago")
@Retry(name = "pedidoService")
public PaymentResponse create(CreatePaymentRequest req) {
    Map<String,Object> pedido = orderClient.getOrder(req.getPedidoId()); // puede fallar
    ...
}
public PaymentResponse fallbackCrearPago(CreatePaymentRequest req, Throwable t) {
    // respuesta degradada controlada
}""")
P("Configuración (sliding window, umbral de fallo, tiempo en estado abierto, reintentos):")
CODE("""resilience4j.circuitbreaker.instances.pedidoService.sliding-window-size=10
resilience4j.circuitbreaker.instances.pedidoService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.pedidoService.wait-duration-in-open-state=10s
resilience4j.retry.instances.pedidoService.max-attempts=3""")
P("Prueba con Python (scripts/ataque.py --target breaker): se bombardea un endpoint que falla "
  "siempre y se observa que responde con FALLBACK (DEGRADED) y código 200, sin caerse.")
SHOT("Salida del script de ataque mostrando respuestas degradadas (fallback)")
PBREAK()

# ===================== ENTREGABLE 11 =====================
H1("Entregable 11. Balanceo de Carga")
P("El Gateway enruta con lb://servicio usando Spring Cloud LoadBalancer sobre el registro de Eureka. "
  "Al ejecutar varias instancias de un servicio, las peticiones se reparten entre ellas; si una cae, "
  "las demás siguen atendiendo.")
H2("Múltiples instancias y prueba")
CODE("""# Levantar 2 instancias de product-service
docker compose -f docker-compose.prod.yml up -d --scale product-service=2

# Monitorear disponibilidad mientras se mata una instancia
python scripts/monitor.py --path "/api/productos?page=0&size=1"
docker kill marketplace-proyecto-product-service-1""")
P("El monitor sigue marcando OK porque la segunda instancia atiende: failover sin caída.")
SHOT("Eureka mostrando 2 instancias del mismo servicio")
SHOT("Monitor en OK tras matar una instancia (failover)")
PBREAK()

# ===================== ENTREGABLE 12 =====================
H1("Entregable 12. Consistencia Distribuida")
P("La consistencia entre servicios se maneja con una saga basada en coreografía de eventos sobre "
  "Kafka: cada servicio reacciona a eventos sin un orquestador central.")
H2("Coreografía (eventos Kafka)")
for e in ["order-service publica “pedido-creado” al crear un pedido.",
          "payment-service, al aprobarse el pago, publica “pago-aprobado” (o “pago-fallido”) y actualiza el estado del pedido.",
          "notification-service consume “pedido-creado”, “pago-aprobado” y “pago-fallido” y genera notificaciones.",
          "recommendation-service consume “resena-creada” para actualizar recomendaciones."]:
    B(e)
CODE("""// Productor (order-service)
kafkaTemplate.send("pedido-creado", evento);

// Consumidor (notification-service)
@KafkaListener(topics = "pago-aprobado", groupId = "notification-service")
public void onPagoAprobado(Map<String,Object> evento) { crearNotificacion(...); }""")
H2("Orquestación y compensación")
P("El flujo de pago actúa como orquestación parcial (payment coordina la actualización del pedido). "
  "La compensación se contempla con los eventos de fallo (“pago-fallido”), que permiten revertir "
  "el estado del pedido a no pagado. (Mejora futura: orquestador de saga dedicado con compensaciones "
  "explícitas por paso.)")
SHOT("Diagrama de la saga (coreografía de eventos)")
SHOT("Tópicos de Kafka y mensajes fluyendo (consola/kafka)")
PBREAK()

# ===================== ENTREGABLE 13 =====================
H1("Entregable 13. Frontend")
P("SPA desarrollada en React 18 + Vite (cumple “Angular o React”). Diseño minimalista con modo "
  "claro/oscuro, optimización de imágenes (Cloudinary f_auto/q_auto) y comunicación con el backend "
  "solo a través del Gateway.")
H2("Funcionalidades implementadas")
for f in ["Login y registro (con validaciones).",
          "Catálogo con búsqueda, chips de categorías y orden (recientes / precio).",
          "Detalle de producto y carrito.",
          "Publicar / editar / eliminar productos (vendedor) con subida de foto.",
          "“Mis productos”, onboarding de vendedor.",
          "Notificaciones (campanita con contador).",
          "Modo oscuro, toasts y skeletons de carga."]:
    B(f)
SHOT("Login")
SHOT("Catálogo (modo claro) con productos y fotos")
SHOT("Catálogo o detalle en modo oscuro")
SHOT("Publicar producto (formulario con subida de foto)")
SHOT("Notificaciones (campanita desplegada)")
SHOT("Carrito de compras")
PBREAK()

# ===================== ENTREGABLE 14 =====================
H1("Entregable 14. Integración Frontend-Backend")
P("El frontend usa un cliente Axios con baseURL relativa: en producción las llamadas van al mismo "
  "origen y Caddy enruta /api/* al Gateway (sin CORS ni mixed-content). Un interceptor añade el JWT "
  "automáticamente a cada petición.")
CODE("""// Interceptor que inyecta el token en cada llamada
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('mp_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});""")
P("Pruebas de integración: el script scripts/smoke-test.sh recorre el flujo completo (registro → "
  "login → producto → pedido → pago → notificación) atravesando el Gateway.")
SHOT("DevTools (Network) mostrando llamadas del SPA al Gateway con el header Authorization")
PBREAK()

# ===================== ENTREGABLE 15 =====================
H1("Entregable 15. Dockerización")
P("Todo el sistema está contenedorizado. Cada microservicio tiene su Dockerfile (build multi-etapa "
  "Maven → JRE) y un docker-compose levanta infraestructura, microservicios, Gateway y frontend.")
H2("Dockerfile (por servicio)")
CODE("""FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]""")
H2("Docker Compose y contenedores")
CODE("""docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps""")
P("Levanta ~22 contenedores: 8 bases de datos, Kafka, Zookeeper, Redis, Eureka, Config, 8 "
  "microservicios, Gateway y el frontend (Caddy). Política restart: unless-stopped para auto-reinicio.")
SHOT("docker compose ps con todos los contenedores Up/healthy")
SHOT("Docker Desktop mostrando los contenedores corriendo")
PBREAK()

# ===================== ENTREGABLE 16 =====================
H1("Entregable 16. Pruebas")
H2("Funcionales / integración")
P("scripts/smoke-test.sh ejecuta el flujo de extremo a extremo vía Gateway (registro, login, crear "
  "producto, pedido, pago, webhook, notificación).")
H2("Seguridad")
P("Verificación de acceso: endpoints protegidos devuelven 401 sin token y 403 con rol insuficiente; "
  "el registro nunca permite auto-asignar ADMIN/SELLER.")
H2("Resiliencia")
P("scripts/ataque.py (carga y circuit breaker) y scripts/monitor.py (autocuración/failover). Ver "
  "DEMO_RESILIENCIA.md para el procedimiento.")
SHOT("Resultado del smoke-test (todas las pruebas OK)")
SHOT("Resultado del script de ataque/resiliencia")
PBREAK()

# ===================== ENTREGABLE 17 =====================
H1("Entregable 17. Manual Técnico")
H2("Arquitectura")
P("Microservicios Spring Boot tras un API Gateway, con descubrimiento (Eureka), configuración "
  "centralizada (Config Server), seguridad JWT, resiliencia (Resilience4j), eventos (Kafka) y "
  "persistencia políglota (PostgreSQL/MySQL/Redis). Ver Resumen técnico al inicio.")
H2("Instalación / requisitos")
B("Docker Desktop (con WSL2 en Windows).")
B("Para desarrollo: JDK 21, Maven, Node 18+.")
H2("Configuración")
P("Variables por entorno (archivo .env): JWT_SECRET, ADMIN_USERNAME/PASSWORD, MAIL_* (verificación), "
  "MERCADOPAGO_ACCESS_TOKEN, SITE_ADDRESS/PUBLIC_URL (despliegue). Valores por defecto para local.")
H2("Despliegue")
P("Local con un comando (docker-compose.prod.yml). En internet: VM (Oracle Cloud free / VPS) + "
  "dominio (DuckDNS) + Caddy con HTTPS automático. Procedimiento completo en DEPLOY.md.")
SHOT("Arquitectura general (diagrama) y/o consola de despliegue")
PBREAK()

# ===================== ENTREGABLE 18 =====================
H1("Entregable 18. Manual de Usuario")
H2("Acceso")
P("Ingresar a la URL del sistema (local: http://localhost). Registrarse o iniciar sesión. "
  "Cuenta de administrador de ejemplo: admin / Admin12345!.")
H2("Funcionalidades")
B("Buscar y filtrar productos por categoría; ordenar por precio o novedad.")
B("Ver detalle y agregar al carrito.")
B("Convertirse en vendedor (“Quiero vender”) y publicar productos con foto.")
B("Gestionar tus productos (editar/eliminar).")
B("Revisar notificaciones en la campanita.")
B("Cambiar entre modo claro y oscuro.")
H2("Casos de uso ilustrados")
SHOT("Pantalla de inicio de sesión")
SHOT("Catálogo y búsqueda")
SHOT("Publicar un producto")
SHOT("Notificaciones del usuario")
PBREAK()

# ===================== ENTREGABLE 19 =====================
H1("Entregable 19. Video de Sustentación")
P("Guion sugerido (8-12 min):")
B("Presentación del sistema y problema que resuelve (1 min).")
B("Arquitectura: C4, microservicios, Gateway, Eureka, Config, Kafka (2-3 min).")
B("Demostración funcional: login, publicar, comprar/pagar, notificaciones (3-4 min).")
B("Resiliencia en vivo: ataque con Python + matar un contenedor y ver recuperación/failover (2 min).")
B("Dockerización: docker compose ps y un solo comando (1 min).")
B("Conclusiones (1 min).")
SHOT("Miniatura / enlace al video")
PBREAK()

# ===================== ENTREGABLE 20 =====================
H1("Entregable 20. Sustentación Final")
H2("Defensa técnica (puntos clave)")
B("Por qué microservicios: independencia, escalabilidad y tolerancia a fallos.")
B("Seguridad: JWT con roles, BCrypt, secretos por entorno, validación en cada servicio.")
B("Resiliencia: Circuit Breaker/Retry/Fallback + auto-reinicio + balanceo con réplicas.")
B("Consistencia: saga por coreografía de eventos con Kafka.")
B("Despliegue: 100% dockerizado, reproducible con un comando.")
H2("Resultados")
P("Sistema funcional de extremo a extremo: 8 microservicios + plataforma, frontend integrado, "
  "pasarela de pago en sandbox, notificaciones por eventos y pruebas de resiliencia demostrables.")
H2("Conclusiones")
P("El proyecto cumple los objetivos de una arquitectura distribuida moderna: separación por dominios, "
  "comunicación síncrona (REST vía Gateway) y asíncrona (Kafka), seguridad centralizada, resiliencia y "
  "despliegue contenedorizado. Queda como mejora futura un orquestador de saga con compensaciones "
  "explícitas y un algoritmo de recomendación más avanzado.")
SHOT("Diapositiva resumen (formato IEEE) / resultados")

# ===================== Documento final =====================
NS = ('xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" '
      'xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"')
document = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            f'<w:document {NS}><w:body>' + "".join(body) +
            '<w:sectPr><w:pgSz w:w="12240" w:h="15840"/>'
            '<w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440"/></w:sectPr>'
            '</w:body></w:document>')

styles = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:sz w:val="22"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:basedOn w:val="Normal"/><w:pPr><w:spacing w:after="200"/></w:pPr><w:rPr><w:b/><w:color w:val="1C1D22"/><w:sz w:val="52"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/><w:pPr><w:outlineLvl w:val="0"/><w:spacing w:before="280" w:after="140"/><w:pBdr><w:bottom w:val="single" w:sz="6" w:space="4" w:color="6366F1"/></w:pBdr></w:pPr><w:rPr><w:b/><w:color w:val="4F46E5"/><w:sz w:val="34"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="heading 2"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/><w:pPr><w:outlineLvl w:val="1"/><w:spacing w:before="200" w:after="100"/></w:pPr><w:rPr><w:b/><w:color w:val="1C1D22"/><w:sz w:val="27"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading3"><w:name w:val="heading 3"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/><w:pPr><w:outlineLvl w:val="2"/><w:spacing w:before="160" w:after="80"/></w:pPr><w:rPr><w:b/><w:color w:val="4B4D57"/><w:sz w:val="24"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Code"><w:name w:val="Code"/><w:basedOn w:val="Normal"/><w:pPr><w:shd w:val="clear" w:color="auto" w:fill="F2F3F5"/><w:spacing w:after="0" w:line="240" w:lineRule="auto"/><w:ind w:left="120" w:right="120"/></w:pPr><w:rPr><w:rFonts w:ascii="Consolas" w:hAnsi="Consolas"/><w:sz w:val="18"/></w:rPr></w:style>
</w:styles>'''

content_types = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>'''

root_rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>'''

doc_rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>'''

with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("[Content_Types].xml", content_types)
    z.writestr("_rels/.rels", root_rels)
    z.writestr("word/document.xml", document)
    z.writestr("word/styles.xml", styles)
    z.writestr("word/_rels/document.xml.rels", doc_rels)

print("OK ->", os.path.abspath(OUT), "bytes:", os.path.getsize(OUT))
