#!/usr/bin/env python3
"""
Ataque de carga (stress test) contra el Marketplace, vía el API Gateway.
Demuestra que el sistema aguanta concurrencia y que el Circuit Breaker
(Resilience4j) degrada con gracia en vez de tumbar el servicio.

Uso:
    python ataque.py                      # ataque al catálogo, 50 hilos, 15 s
    python ataque.py --target login       # ataca el login
    python ataque.py --target breaker     # dispara el Circuit Breaker (pagos)
    python ataque.py --hilos 100 --segundos 30

Solo usa la librería estándar de Python (no requiere pip install).
"""
import argparse
import json
import threading
import time
import urllib.request
import urllib.error

BASE = "http://127.0.0.1"          # API Gateway (puerto 80 vía Caddy); IPv4 explícito para Windows
ADMIN_USER = "admin"
ADMIN_PASS = "Admin12345!"

# Contadores compartidos
lock = threading.Lock()
stats = {"ok": 0, "fail": 0, "degraded": 0, "lat_total": 0.0, "codes": {}}
stop_flag = threading.Event()


def http(method, path, token=None, body=None, timeout=10):
    url = BASE + path
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    start = time.time()
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            text = resp.read().decode(errors="ignore")
            return resp.status, text, time.time() - start
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode(errors="ignore"), time.time() - start
    except Exception as e:
        return 0, str(e), time.time() - start


def login():
    code, text, _ = http("POST", "/api/auth/login",
                          body={"username": ADMIN_USER, "password": ADMIN_PASS})
    if code == 200:
        return json.loads(text).get("token")
    print(f"[!] No se pudo loguear (HTTP {code}): {text[:120]}")
    return None


def worker(target, token):
    while not stop_flag.is_set():
        if target == "login":
            code, text, lat = http("POST", "/api/auth/login",
                                    body={"username": ADMIN_USER, "password": ADMIN_PASS})
        elif target == "breaker":
            # Endpoint que SIEMPRE falla en el servidor -> Resilience4j responde con fallback
            code, text, lat = http("GET", "/api/pagos/test-resilience", token=token)
        else:  # catalogo
            code, text, lat = http("GET", "/api/productos?page=0&size=10", token=token)

        with lock:
            stats["codes"][code] = stats["codes"].get(code, 0) + 1
            stats["lat_total"] += lat
            if 200 <= code < 300:
                stats["ok"] += 1
                if "DEGRAD" in text.upper() or "fallback" in text.lower():
                    stats["degraded"] += 1
            else:
                stats["fail"] += 1


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--target", choices=["catalogo", "login", "breaker"], default="catalogo")
    ap.add_argument("--hilos", type=int, default=50)
    ap.add_argument("--segundos", type=int, default=15)
    args = ap.parse_args()

    print(f"== Ataque de carga ==  objetivo={args.target}  hilos={args.hilos}  duracion={args.segundos}s")
    token = login()
    if token:
        print("[+] Token obtenido, empezando el bombardeo...\n")
    else:
        print("[!] Sin token; algunos endpoints pueden devolver 401.\n")

    hilos = [threading.Thread(target=worker, args=(args.target, token), daemon=True)
             for _ in range(args.hilos)]
    t0 = time.time()
    for h in hilos:
        h.start()
    time.sleep(args.segundos)
    stop_flag.set()
    for h in hilos:
        h.join(timeout=5)
    dur = time.time() - t0

    total = stats["ok"] + stats["fail"]
    rps = total / dur if dur else 0
    lat_ms = (stats["lat_total"] / total * 1000) if total else 0
    print("\n================ RESULTADO ================")
    print(f"  Peticiones totales : {total}")
    print(f"  OK (2xx)           : {stats['ok']}")
    print(f"  Fallidas           : {stats['fail']}")
    print(f"  Respuestas degradadas (fallback): {stats['degraded']}")
    print(f"  Requests/segundo   : {rps:.1f}")
    print(f"  Latencia media     : {lat_ms:.1f} ms")
    print(f"  Códigos HTTP       : {dict(sorted(stats['codes'].items()))}")
    print("===========================================")
    if args.target == "breaker" and stats["degraded"]:
        print("El Circuit Breaker respondió con FALLBACK: el servicio degradó con gracia,")
        print("no se cayó pese a que la dependencia interna fallaba en cada llamada.")


if __name__ == "__main__":
    main()
