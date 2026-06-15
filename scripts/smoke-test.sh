#!/usr/bin/env bash
# Smoke test e2e del Marketplace contra el API Gateway (:8080).
# Requisitos: infra y servicios arriba (ver GUIA_ARRANQUE.md). Solo necesita curl.
# Uso:  bash scripts/smoke-test.sh
set -u

BASE="${BASE_URL:-http://localhost:8080}"
PASS=0; FAIL=0

token_of() { sed -n 's/.*"token":"\([^"]*\)".*/\1/p'; }
field()    { sed -n "s/.*\"$1\":\"\?\([^\",}]*\)\"\?.*/\1/p"; }

check() { # check <desc> <http_code>
  if [ "$2" -ge 200 ] && [ "$2" -lt 300 ]; then echo "  OK  ($2) $1"; PASS=$((PASS+1));
  else echo "  FAIL($2) $1"; FAIL=$((FAIL+1)); fi
}

echo "== 1) Login ADMIN =="
ADMIN_TOKEN=$(curl -s -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin12345!"}' | token_of)
[ -n "$ADMIN_TOKEN" ] && echo "  OK  token admin" || { echo "  FAIL no admin token"; exit 1; }

echo "== 2) Registro USER (ignora si ya existe) =="
curl -s -o /dev/null -X POST "$BASE/api/auth/register" -H 'Content-Type: application/json' \
  -d '{"fullName":"Juan Perez","dni":"12345678","email":"juan@upeu.edu.pe","universityCode":"202012345","phone":"987654321","username":"juanp","password":"Password123!"}'

echo "== 3) Login USER =="
USER_TOKEN=$(curl -s -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' \
  -d '{"username":"juanp","password":"Password123!"}' | token_of)
[ -n "$USER_TOKEN" ] && echo "  OK  token user" || echo "  FAIL no user token"

echo "== 4) (ADMIN) Crear categoria =="
CODE=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/api/categorias" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"SmokeTest","description":"cat de prueba"}')
check "crear categoria (ADMIN)" "$CODE"

echo "== 5) (ADMIN) Crear producto =="
PROD=$(curl -s -X POST "$BASE/api/productos" -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Producto Smoke","description":"x","price":50.0,"stock":10,"sellerId":"11111111-1111-1111-1111-111111111111","categoryId":1}')
PROD_ID=$(echo "$PROD" | field id)
[ -n "$PROD_ID" ] && echo "  OK  producto id=$PROD_ID" || echo "  FAIL no se creo producto: $PROD"

echo "== 6) Listar productos (publico) =="
CODE=$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/productos")
check "listar productos" "$CODE"

echo "== 7) Crear pedido (USER) =="
PED=$(curl -s -X POST "$BASE/api/pedidos" -H "Authorization: Bearer $USER_TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"compradorId\":\"22222222-2222-2222-2222-222222222222\",\"vendedorId\":\"11111111-1111-1111-1111-111111111111\",\"items\":[{\"productoId\":${PROD_ID:-1},\"tituloProducto\":\"Producto Smoke\",\"quantity\":1,\"precioUnitario\":50.0}]}")
PED_ID=$(echo "$PED" | field id)
[ -n "$PED_ID" ] && echo "  OK  pedido id=$PED_ID" || echo "  FAIL no se creo pedido: $PED"

echo "== 8) Crear pago (USER) =="
PAGO=$(curl -s -X POST "$BASE/api/pagos" -H "Authorization: Bearer $USER_TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"pedidoId\":\"$PED_ID\",\"compradorId\":\"22222222-2222-2222-2222-222222222222\",\"monto\":50.0}")
echo "  pago: $PAGO"

echo "== 9) Confirmar pago via webhook (publico) =="
CODE=$(curl -s -o /dev/null -w '%{http_code}' -X POST \
  "$BASE/api/pagos/webhook/mercadopago?externalReference=mp-order-$PED_ID&status=approved")
check "webhook approved" "$CODE"

echo "== 10) Verificar pedido =="
CODE=$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/pedidos/$PED_ID" -H "Authorization: Bearer $USER_TOKEN")
check "consultar pedido" "$CODE"

echo "== 11) Reseñas del producto (publico) =="
CODE=$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/resenas?productId=${PROD_ID:-1}")
check "listar resenas" "$CODE"

echo "== 12) Notificaciones del comprador =="
CODE=$(curl -s -o /dev/null -w '%{http_code}' \
  "$BASE/api/notificaciones?usuarioId=22222222-2222-2222-2222-222222222222" -H "Authorization: Bearer $USER_TOKEN")
check "listar notificaciones" "$CODE"

echo
echo "===== RESULTADO: $PASS OK, $FAIL FAIL ====="
[ "$FAIL" -eq 0 ]
