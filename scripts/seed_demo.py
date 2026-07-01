#!/usr/bin/env python3
"""
Siembra datos de demostración en el Marketplace (vía el API Gateway):
  - Crea varios usuarios y los convierte en vendedores.
  - Cada vendedor publica productos con FOTOS reales (picsum.photos).
  - Crea notificaciones para el admin (para que la campanita tenga datos).

Uso:
    python seed_demo.py
Solo usa la librería estándar (no requiere pip install).
Es idempotente-ish: si un usuario ya existe, lo reutiliza (login).
"""
import base64
import json
import urllib.request
import urllib.error

BASE = "http://127.0.0.1"  # IPv4 explícito (en Windows 'localhost' puede ir a IPv6 y fallar)
ADMIN_USER, ADMIN_PASS = "admin", "Admin12345!"


def http(method, path, token=None, body=None):
    url = BASE + path
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req, timeout=15) as r:
            txt = r.read().decode(errors="ignore")
            return r.status, (json.loads(txt) if txt.strip().startswith(("{", "[")) else txt)
    except urllib.error.HTTPError as e:
        txt = e.read().decode(errors="ignore")
        try:
            return e.code, json.loads(txt)
        except Exception:
            return e.code, txt
    except Exception as e:
        return 0, str(e)


def user_id_from_token(token):
    """Lee el claim userId del JWT (sin verificar firma)."""
    payload = token.split(".")[1]
    payload += "=" * (-len(payload) % 4)  # padding
    claims = json.loads(base64.urlsafe_b64decode(payload))
    return claims.get("userId")


def login(username, password):
    code, data = http("POST", "/api/auth/login", body={"username": username, "password": password})
    if code == 200 and isinstance(data, dict):
        return data.get("token")
    return None


# Usuarios de demostración (datos válidos: dni 8, código 9, celular 9)
USERS = [
    {"fullName": "Ana Torres",  "username": "ana",   "dni": "10000001", "email": "ana@upeu.edu.pe",   "universityCode": "202000001", "phone": "900000001", "password": "Password1!"},
    {"fullName": "Luis Rojas",  "username": "luis",  "dni": "10000002", "email": "luis@upeu.edu.pe",  "universityCode": "202000002", "phone": "900000002", "password": "Password1!"},
    {"fullName": "María Díaz",  "username": "maria", "dni": "10000003", "email": "maria@upeu.edu.pe", "universityCode": "202000003", "phone": "900000003", "password": "Password1!"},
    {"fullName": "José Quispe", "username": "jose",  "dni": "10000004", "email": "jose@upeu.edu.pe",  "universityCode": "202000004", "phone": "900000004", "password": "Password1!"},
]

# Productos por vendedor (nombre, descripción, precio, stock, slug-imagen)
PRODUCTS = [
    [("Laptop usada i5",      "Ideal para clases, 8GB RAM",        1200.0, 3, "laptop"),
     ("Mouse inalámbrico",    "Ergonómico, poco uso",                45.0, 10, "mouse")],
    [("Cálculo de Stewart",   "Libro de cálculo, 8va edición",       80.0, 5, "calculusbook"),
     ("Mochila Jansport",     "Resistente, color azul",             150.0, 4, "backpack")],
    [("Audífonos gamer",      "Con micrófono, sonido envolvente",   130.0, 6, "headset"),
     ("Cafetera personal",    "Para sobrevivir los finales",         99.0, 7, "coffee")],
    [("Bicicleta urbana",     "Para moverte por el campus",         600.0, 2, "bike"),
     ("Camiseta universitaria","Talla M, edición 2026",              40.0, 20, "tshirt")],
]


def main():
    print("== Sembrando datos de demostración ==\n")
    admin = login(ADMIN_USER, ADMIN_PASS)
    if not admin:
        print("[!] No pude entrar como admin. ¿Está el backend arriba en http://localhost?")
        return

    # Categorías existentes (las creó el seeder del product-service)
    _, cats = http("GET", "/api/categorias", token=admin)
    cat_ids = [c["id"] for c in cats] if isinstance(cats, list) and cats else [1, 2, 3]
    print(f"[+] Categorías disponibles: {cat_ids}\n")

    total_products = 0
    for i, u in enumerate(USERS):
        # Registrar (si ya existe, seguimos con login)
        http("POST", "/api/auth/register", body=u)
        token = login(u["username"], u["password"])
        if not token:
            print(f"[!] No pude loguear a {u['username']}, lo salto.")
            continue
        # Convertir en vendedor (devuelve token nuevo con rol SELLER)
        code, data = http("POST", "/api/auth/become-seller", token=token)
        if code == 200 and isinstance(data, dict) and data.get("token"):
            token = data["token"]
        print(f"[+] {u['username']} listo como vendedor")

        for (name, desc, price, stock, slug) in PRODUCTS[i]:
            body = {
                "name": name, "description": desc, "price": price, "stock": stock,
                "categoryId": cat_ids[i % len(cat_ids)],
                "imageUrl": f"https://picsum.photos/seed/{slug}/600/400",
            }
            code, data = http("POST", "/api/productos", token=token, body=body)
            if 200 <= code < 300:
                total_products += 1
                print(f"      · publicado: {name}")
            else:
                print(f"      · ERROR publicando {name} (HTTP {code}): {data}")

    # Notificaciones para el admin (para que la campanita tenga datos).
    # Evita duplicar si ya se sembraron antes.
    admin_id = user_id_from_token(admin)
    _, existing = http("GET", f"/api/notificaciones?usuarioId={admin_id}", token=admin)
    n_ok = len(existing) if isinstance(existing, list) else 0
    if n_ok >= 5:
        print("\n[=] El admin ya tiene notificaciones, no se duplican.")
    else:
        notifs = [
            ("SYSTEM",  "¡Bienvenido a Feria!", "Tu cuenta de administrador está lista."),
            ("ORDER",   "Nuevo pedido",         "Tienes un pedido pendiente de revisar."),
            ("PAYMENT", "Pago aprobado",        "Se aprobó un pago por S/ 150.00."),
            ("REVIEW",  "Nueva reseña",         "Un comprador dejó 5 estrellas en tu producto."),
            ("MESSAGE", "Mensaje nuevo",        "Ana te preguntó por la laptop."),
        ]
        for tipo, titulo, msg in notifs:
            code, _ = http("POST", "/api/notificaciones", token=admin, body={
                "usuarioId": admin_id, "tipo": tipo, "titulo": titulo, "mensaje": msg, "referenciaId": "demo",
            })
            if 200 <= code < 300:
                n_ok += 1

    print("\n================ RESUMEN ================")
    print(f"  Vendedores creados   : {len(USERS)}")
    print(f"  Productos publicados : {total_products}")
    print(f"  Notificaciones admin : {n_ok}")
    print("=========================================")
    print("Abre http://localhost, entra como admin y mira el catálogo y la campanita 🔔")


if __name__ == "__main__":
    main()
