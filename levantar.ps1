# =====================================================================
#  Levanta TODO el marketplace con Docker (Windows / PowerShell).
#  Uso:   .\levantar.ps1
#  Si PowerShell bloquea el script:
#         powershell -ExecutionPolicy Bypass -File .\levantar.ps1
# =====================================================================
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$COMPOSE = "docker-compose.prod.yml"

# 1) Verifica que Docker esté corriendo.
try {
    docker info *> $null
} catch {
    Write-Host "[X] Docker no está corriendo. Abre Docker Desktop y espera a que diga 'Running'." -ForegroundColor Red
    exit 1
}

# 2) Asegura un JWT_SECRET (mínimo 32 caracteres) vía archivo .env.
if (-not (Test-Path ".env")) {
    "JWT_SECRET=clave_secreta_dev_marketplace_2026_minimo_32_chars" | Out-File -FilePath ".env" -Encoding ascii
    Write-Host "[+] Creado .env con un JWT_SECRET por defecto." -ForegroundColor Green
}

# 3) Construye y levanta todo en segundo plano.
Write-Host "`n[*] Construyendo y levantando el stack (la primera vez tarda varios minutos)..." -ForegroundColor Cyan
docker compose -f $COMPOSE up -d --build

# 4) Muestra el estado.
Write-Host "`n[*] Estado de los contenedores:" -ForegroundColor Cyan
docker compose -f $COMPOSE ps

Write-Host "`n==============================================================" -ForegroundColor Yellow
Write-Host "  App (frontend):  http://localhost" -ForegroundColor Yellow
Write-Host "  Login admin:     admin / Admin12345!" -ForegroundColor Yellow
Write-Host "==============================================================" -ForegroundColor Yellow
Write-Host "Dale 1-2 minutos a que TODOS los servicios terminen de arrancar."
Write-Host "Para ver logs:   docker compose -f $COMPOSE logs -f"
Write-Host "Para apagar:     docker compose -f $COMPOSE down"
