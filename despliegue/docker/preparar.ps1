<#
  SISCO / FADIDE · Escenario 4 (Docker): compila el backend y el frontend, copia los
  artefactos a cada servidor y levanta los 3 contenedores.

  Uso (PowerShell, desde cualquier carpeta):
    .\despliegue\docker\preparar.ps1                                   # todo
    .\despliegue\docker\preparar.ps1 -Frontend "D:\ruta\FADIDE---Frontend"
    .\despliegue\docker\preparar.ps1 -SinCompilar                      # solo reconstruir contenedores
#>
param(
    [string]$Frontend = $(if ($env:SISCO_FRONTEND) { $env:SISCO_FRONTEND } else {
        Join-Path $HOME 'Documents\8vo Ciclo\Soluciones Web y Aplicaciones Distribuidas\FADIDE---Frontend' }),
    [switch]$SinCompilar
)
# 'Continue': Maven, npm y Docker escriben avisos en stderr que en Windows PowerShell 5.1
# se tomarían como errores; el resultado de cada paso se valida con $LASTEXITCODE.
$ErrorActionPreference = 'Continue'
$docker  = $PSScriptRoot
$backend = (Resolve-Path (Join-Path $docker '..\..')).Path

function Paso($texto) { Write-Host "`n==> $texto" -ForegroundColor DarkYellow }

Paso 'Verificando Docker'
docker version --format 'Docker Engine {{.Server.Version}}'
if ($LASTEXITCODE -ne 0) { throw 'Docker Desktop no está iniciado.' }

if (-not $SinCompilar) {
    Paso 'Compilando el backend (sisco.war)'
    if (-not $env:JAVA_HOME) {
        $jbr = Get-ChildItem 'C:\Program Files\JetBrains' -Directory -ErrorAction SilentlyContinue |
               ForEach-Object { Join-Path $_.FullName 'jbr' } | Where-Object { Test-Path $_ } | Select-Object -First 1
        if ($jbr) { $env:JAVA_HOME = $jbr } else { throw 'Defina JAVA_HOME (JDK 17 o superior).' }
    }
    Push-Location $backend
    & .\mvnw.cmd -q -DskipTests clean package
    if ($LASTEXITCODE -ne 0) { Pop-Location; throw 'Falló la compilación del backend.' }
    Pop-Location

    Paso "Compilando el frontend (ng build --configuration docker) en $Frontend"
    Push-Location $Frontend
    npx ng build --configuration docker
    if ($LASTEXITCODE -ne 0) { Pop-Location; throw 'Falló la compilación del frontend.' }
    Pop-Location
}

Paso 'Copiando artefactos a cada servidor'
Copy-Item (Join-Path $backend 'target\sisco.war') (Join-Path $docker 'backend\sisco.war') -Force
Copy-Item (Join-Path $backend 'despliegue\postgresql\datos-demo.sql') (Join-Path $docker 'backend\datos-demo.sql') -Force
$dist = Join-Path $docker 'frontend\dist'
if (Test-Path $dist) { Remove-Item $dist -Recurse -Force }
Copy-Item (Join-Path $Frontend 'dist\sisco-frontend\browser') $dist -Recurse

$envFile = Join-Path $docker '.env'
if (-not (Test-Path $envFile)) {
    Paso 'Generando .env con claves aleatorias'
    function Clave($n) { -join ((48..57) + (65..90) + (97..122) | Get-Random -Count $n | ForEach-Object { [char]$_ }) }
    @"
DB_USERNAME=sisco
DB_PASSWORD=$(Clave 20)
JWT_SECRET=$(Clave 48)
ADMIN_CORREO=admin@fadide.com
ADMIN_PASSWORD=Admin123.
CARGAR_DATOS_DEMO=true
"@ | Set-Content -Path $envFile -Encoding ascii
}

Paso 'Construyendo y levantando los contenedores'
Push-Location $docker
docker compose up -d --build
$codigo = $LASTEXITCODE
docker compose ps
Pop-Location
if ($codigo -ne 0) { throw 'docker compose up falló.' }

Write-Host "`nListo. Frontend: http://localhost:8081   Backend: http://localhost:8080/sisco/swagger-ui/index.html" -ForegroundColor Green
Write-Host 'El backend tarda ~1-2 minutos en desplegar sisco.war la primera vez (docker compose logs -f backend).'
