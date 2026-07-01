#!/usr/bin/env python3
"""
Monitor de disponibilidad para demostrar AUTOCURACIÓN y FAILOVER.

Pinguea un endpoint del gateway cada 0.5 s y muestra OK / FALLO con la hora.
Déjalo corriendo en una terminal y, en otra, mata un microservicio:

    docker kill marketplace-proyecto-product-service-1

Verás cómo empieza a FALLAR y, segundos después, se RECUPERA solo
(porque el compose tiene restart: unless-stopped, o porque hay otra réplica).

Uso:
    python monitor.py                       # monitorea el catálogo (product-service)
    python monitor.py --path /api/productos
    python monitor.py --path /api/auth/health/external
Solo usa la librería estándar de Python.
"""
import argparse
import time
import urllib.request
import urllib.error
from datetime import datetime

BASE = "http://127.0.0.1"  # IPv4 explícito (en Windows 'localhost' puede ir a IPv6 y fallar)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--path", default="/api/productos?page=0&size=1",
                    help="endpoint a vigilar (relativo al gateway)")
    ap.add_argument("--intervalo", type=float, default=0.5)
    args = ap.parse_args()

    print(f"Vigilando {BASE}{args.path}  (Ctrl+C para parar)\n")
    ok_streak = 0
    fail_streak = 0
    while True:
        t = datetime.now().strftime("%H:%M:%S")
        start = time.time()
        try:
            req = urllib.request.Request(BASE + args.path, method="GET")
            with urllib.request.urlopen(req, timeout=3) as resp:
                lat = (time.time() - start) * 1000
                ok_streak += 1
                fail_streak = 0
                print(f"[{t}]  OK    HTTP {resp.status}   {lat:6.0f} ms   (seguidas: {ok_streak})")
        except urllib.error.HTTPError as e:
            # 401/403 igual significan que el servicio RESPONDE (está vivo)
            lat = (time.time() - start) * 1000
            ok_streak += 1
            fail_streak = 0
            print(f"[{t}]  OK*   HTTP {e.code}   {lat:6.0f} ms   (responde, requiere auth)")
        except Exception as e:
            fail_streak += 1
            ok_streak = 0
            print(f"[{t}]  CAÍDO  {type(e).__name__}   (fallos seguidos: {fail_streak})  <- servicio no responde")
        time.sleep(args.intervalo)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\nMonitor detenido.")
