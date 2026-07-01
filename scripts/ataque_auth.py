#!/usr/bin/env python3
"""
Ataque de carga POTENTE y dirigido a auth-service (para verlo en Grafana).

Por qué pega tan fuerte:
  - Le da DIRECTO a auth-service en el puerto 8081, saltándose el API Gateway
    y su rate limiting (si fueras por el gateway, te cortaría con 429).
  - Martillea /api/auth/login. Cada intento ejecuta BCrypt (verificación de
    contraseña), que consume CPU: verás picos claros de CPU, latencia y
    peticiones/seg en los dashboards.
  - Alta concurrencia con muchos hilos y estadísticas en vivo cada segundo,
    para que puedas mirar Grafana mientras corre.

Uso:
    python ataque_auth.py                          # 200 hilos, 30 s, directo a :8081
    python ataque_auth.py --hilos 400 --segundos 60
    python ataque_auth.py --via-gateway            # por el gateway (probará el rate limit → 429)
    python ataque_auth.py --registro               # además crea usuarios (BCrypt en el guardado)

Solo librería estándar de Python (no requiere pip install).
"""
import argparse
import json
import random
import string
import threading
import time
import urllib.request
import urllib.error

# Directo a auth-service (salta el gateway y el rate limiting).
DIRECT_BASE = "http://127.0.0.1:8081"
GATEWAY_BASE = "http://127.0.0.1"        # gateway en el puerto 80 (aquí SÍ aplica el rate limit)

ADMIN_USER = "admin"
ADMIN_PASS = "Admin12345!"

lock = threading.Lock()
stats = {"total": 0, "ok": 0, "fail": 0, "lat_total": 0.0, "codes": {}}
last_snapshot = {"total": 0, "t": time.time()}
stop_flag = threading.Event()


def http(base, method, path, body=None, timeout=10):
    url = base + path
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    start = time.time()
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            resp.read()
            return resp.status, time.time() - start
    except urllib.error.HTTPError as e:
        try:
            e.read()
        except Exception:
            pass
        return e.code, time.time() - start
    except Exception:
        return 0, time.time() - start


def rand_user(n=10):
    return "".join(random.choices(string.ascii_lowercase, k=n))


def record(code, lat):
    with lock:
        stats["total"] += 1
        stats["lat_total"] += lat
        stats["codes"][code] = stats["codes"].get(code, 0) + 1
        if 200 <= code < 300:
            stats["ok"] += 1
        else:
            stats["fail"] += 1


def worker(base, do_register):
    while not stop_flag.is_set():
        r = random.random()
        if do_register and r < 0.15:
            # Registro: fuerza un hash BCrypt al guardar el usuario nuevo.
            u = rand_user()
            code, lat = http(base, "POST", "/api/auth/register", body={
                "fullName": "Carga Demo", "username": u, "dni": "10" + "".join(random.choices(string.digits, k=6)),
                "email": u + "@upeu.edu.pe", "universityCode": "20" + "".join(random.choices(string.digits, k=7)),
                "phone": "9" + "".join(random.choices(string.digits, k=8)), "password": "Password1!",
            })
        elif r < 0.85:
            # Login con contraseña MALA de un usuario que SÍ existe (admin):
            # obliga a auth-service a ejecutar BCrypt (lo más caro) y responde 401.
            code, lat = http(base, "POST", "/api/auth/login", body={
                "username": ADMIN_USER, "password": rand_user(12)})
        else:
            # Login válido (200 + token). También ejecuta BCrypt.
            code, lat = http(base, "POST", "/api/auth/login", body={
                "username": ADMIN_USER, "password": ADMIN_PASS})
        record(code, lat)


def reporter():
    """Imprime estadísticas EN VIVO cada segundo (para mirar Grafana en paralelo)."""
    while not stop_flag.is_set():
        time.sleep(1)
        with lock:
            total = stats["total"]
            codes = dict(sorted(stats["codes"].items()))
            lat_ms = (stats["lat_total"] / total * 1000) if total else 0
        now = time.time()
        delta = total - last_snapshot["total"]
        rps = delta / (now - last_snapshot["t"]) if now > last_snapshot["t"] else 0
        last_snapshot["total"] = total
        last_snapshot["t"] = now
        print(f"[{time.strftime('%H:%M:%S')}]  {rps:7.0f} req/s   total={total:<8}  lat~{lat_ms:5.0f} ms   {codes}")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--hilos", type=int, default=200)
    ap.add_argument("--segundos", type=int, default=30)
    ap.add_argument("--via-gateway", action="store_true",
                    help="Ataca por el gateway (puerto 80). Probará el rate limiting (429).")
    ap.add_argument("--registro", action="store_true",
                    help="Incluye registros (hash BCrypt al guardar).")
    args = ap.parse_args()

    base = GATEWAY_BASE if args.via_gateway else DIRECT_BASE
    destino = "GATEWAY :80 (con rate limit)" if args.via_gateway else "auth-service :8081 (directo)"

    print("=" * 62)
    print(f"  ATAQUE a auth-service")
    print(f"  destino : {base}   [{destino}]")
    print(f"  hilos   : {args.hilos}    duracion: {args.segundos}s")
    print(f"  carga   : login BCrypt (admin) + {'registros + ' if args.registro else ''}logins validos")
    print("=" * 62)
    print("  Abre Grafana (http://localhost:3001) y mira los picos en vivo:\n")

    hilos = [threading.Thread(target=worker, args=(base, args.registro), daemon=True)
             for _ in range(args.hilos)]
    rep = threading.Thread(target=reporter, daemon=True)

    t0 = time.time()
    for h in hilos:
        h.start()
    rep.start()
    time.sleep(args.segundos)
    stop_flag.set()
    for h in hilos:
        h.join(timeout=5)
    dur = time.time() - t0

    total = stats["total"]
    rps = total / dur if dur else 0
    lat_ms = (stats["lat_total"] / total * 1000) if total else 0
    print("\n================ RESULTADO ================")
    print(f"  Peticiones totales : {total}")
    print(f"  OK (2xx)           : {stats['ok']}")
    print(f"  Rechazadas/errores : {stats['fail']}")
    print(f"  Requests/segundo   : {rps:.0f}")
    print(f"  Latencia media     : {lat_ms:.1f} ms")
    print(f"  Codigos HTTP       : {dict(sorted(stats['codes'].items()))}")
    print("===========================================")
    if args.via_gateway and stats["codes"].get(429):
        print("Aparecieron 429: el RATE LIMITING del gateway corto el exceso de trafico.")
    if not args.via_gateway:
        print("Mira en Grafana el pico de CPU/latencia de auth-service durante el ataque.")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        stop_flag.set()
        print("\nAtaque detenido.")
