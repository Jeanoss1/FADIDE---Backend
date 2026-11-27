# Despliegue distribuido de SISCO — Escenario 4 (Docker)

Escenario elegido por el equipo entre los cuatro propuestos (1 PC local, 3 máquinas virtuales,
3 máquinas físicas, Docker). Se conserva la misma arquitectura de tres servidores, cada uno con su
propio sistema operativo y un único rol, pero como **contenedores** en una red privada de Docker.

```
 Equipo anfitrión (Windows 11 + Docker Desktop)        Red Docker "sisco-red" 172.20.56.0/24
 Navegador ──HTTP──▶ localhost:8081 ──▶ sisco-frontend  172.20.56.30  AlmaLinux 9 + Apache httpd 2.4
 Navegador ──HTTP──▶ localhost:8080 ──▶ sisco-backend   172.20.56.20  Ubuntu 24.04 + OpenJDK 17 + Tomcat 11 (sisco.war)
                                             └──JDBC:5432──▶ sisco-bd   172.20.56.10  Rocky Linux 9 + PostgreSQL 18
```

## Por qué Docker y no máquinas virtuales

| Criterio | 3 VMs (VirtualBox) | Docker (elegido) |
|---|---|---|
| RAM usada | ~3 GB | ~0.6 GB (BD 55 MB, backend ~450 MB, frontend 26 MB) |
| Disco | ~18 GB + 7 GB de ISOs | ~2.5 GB de imágenes |
| Tiempo de puesta en marcha | horas (instalar 3 SO) | ~5 min (`preparar.ps1`) |
| Reproducibilidad | manual, paso a paso | idéntica en cualquier equipo con Docker |
| Aislamiento | SO completo por VM | proceso, red e IP propios por contenedor (kernel compartido) |

Con un equipo de 8 GB de RAM y ~40 GB libres, Docker permite levantar los tres servidores a la vez
sin degradar el equipo y repetir el despliegue el día de la sustentación con un solo comando.

## Qué se instala en cada servidor (todo por terminal, en los Dockerfile)

| Servidor | Dockerfile | Instalación |
|---|---|---|
| **sisco-bd** | `despliegue/docker/bd/Dockerfile` | `dnf install` PostgreSQL 18 desde el repositorio oficial PGDG · `initdb` · `listen_addresses='*'` · `pg_hba.conf` solo admite al usuario `sisco` desde **172.20.56.20** con `scram-sha-256` · crea `sisco_db` |
| **sisco-backend** | `despliegue/docker/backend/Dockerfile` | `apt install openjdk-17-jre-headless` · descarga **Tomcat 11.0.29 portable** (`.tar.gz`) en `/opt/tomcat` · usuario sin privilegios `tomcat` · `bin/setenv.sh` (JVM) · `sisco.war` en `webapps` · arranque con **`bin/startup.sh`** y parada con `bin/shutdown.sh` · carga `datos-demo.sql` con `psql` |
| **sisco-frontend** | `despliegue/docker/frontend/Dockerfile` | `dnf install httpd` · `sisco.conf` (VirtualHost, `AllowOverride All`) · build de Angular en `/var/www/html` con su `.htaccess` (rutas de Angular y cabeceras de seguridad) |

Los archivos de evidencia que suben los supervisores (imágenes, PDF, Word, Excel, PowerPoint, DWG, texto; máx. 10 MB) se guardan en el volumen `sisco-evidencias` (`/var/lib/sisco/evidencias`), por lo que sobreviven a los redespliegues.

Las credenciales (BD, JWT, administrador) **no están en las imágenes ni en el repositorio**: viajan como
variables de entorno desde `despliegue/docker/.env` (ignorado por Git; plantilla en `.env.example`).

## Requisitos

- Windows 10/11 con **Docker Desktop** iniciado (*Engine running*).
- JDK 17+ (o el JBR de IntelliJ, que el script detecta solo) y Node.js con las dependencias del
  frontend instaladas (`npm install`).
- Puertos libres en el anfitrión: **8080** (backend) y **8081** (frontend). La BD no publica puertos.
  El puerto 80 de este equipo lo ocupa Windows, por eso el frontend se publica en 8081.

## Paso a paso

```powershell
# 1. Compilar backend (WAR) y frontend (ng build --configuration docker), copiar artefactos,
#    generar .env con claves aleatorias y levantar los 3 contenedores
cd C:\Users\LENOVO\IdeaProjects\FADIDE---Backend
.\despliegue\docker\preparar.ps1

# 2. Ver el estado: los 3 deben quedar "healthy" (el backend tarda ~1 min la primera vez)
cd despliegue\docker
docker compose ps
docker compose logs -f backend        # Ctrl+C para salir
```

`preparar.ps1` hace, en orden:
1. `mvnw clean package -DskipTests` → `target/sisco.war`.
2. `ng build --configuration docker` → build que apunta a `http://localhost:8080/sisco/api`.
3. Copia `sisco.war`, `datos-demo.sql` y el build a las carpetas de cada servidor.
4. Crea `.env` si no existe.
5. `docker compose up -d --build`.

### Operación diaria

| Acción | Comando (en `despliegue/docker`) |
|---|---|
| Levantar (sin recompilar) | `docker compose up -d` |
| Detener (los datos se conservan) | `docker compose down` |
| Reiniciar solo el backend | `docker compose restart backend` |
| Desplegar un nuevo WAR / build | `..\..\despliegue\docker\preparar.ps1` |
| Borrar TODO, incluida la BD y los archivos de evidencia | `docker compose down -v` |
| Entrar a un servidor | `docker exec -it sisco-backend bash` |

## Acceso

| Qué | URL |
|---|---|
| **Sistema (frontend)** | http://localhost:8081 |
| API REST | http://localhost:8080/sisco/api |
| Documentación Swagger | http://localhost:8080/sisco/swagger-ui/index.html |
| Base de datos | sin puerto publicado: solo desde la red interna o con `docker exec` (ver abajo) |

| Usuario | Contraseña | Rol |
|---|---|---|
| admin@fadide.com | Admin123. | ADMIN |
| gestor@fadide.com | Gestor123. | GESTOR |
| supervisor@fadide.com | Supervisor123. | SUPERVISOR |
| supervisor2@fadide.com | Supervisor123. | SUPERVISOR |

Consultas directas a la BD dentro de su servidor:
```powershell
docker exec -it sisco-bd psql -d sisco_db
#   \dt                                   tablas creadas por Hibernate
#   select correo, activo from usuarios;
```

## Pruebas realizadas

| # | Prueba | Resultado esperado |
|---|---|---|
| 1 | `docker compose ps` | 3 contenedores `Up (healthy)` |
| 2 | http://localhost:8081 y recargar (F5) en `/pages/cliente` | 200, sin 404 (`.htaccess`) |
| 3 | Login con los 3 roles | Dashboard distinto por rol (KPI y gráficos) |
| 4 | Registrar un cliente en el frontend | Aparece en `docker exec sisco-bd psql -d sisco_db -c "select * from clientes"` |
| 5 | Reportes → exportar PDF y Excel | Descarga de `.pdf` y `.xlsx` |
| 6 | `docker exec sisco-backend bash -c 'PGPASSWORD=x psql -h 172.20.56.10 -U sisco -d postgres'` | Rechazado por `pg_hba.conf` (solo `sisco_db`) |
| 7 | `curl -X OPTIONS` con `Origin: http://localhost:8081` | `Access-Control-Allow-Origin: http://localhost:8081` |

## Evidencias para el informe (sección 5.7)

Capturas de **pantalla completa** con la fecha y hora de Windows visibles en la barra de tareas:

1. Docker Desktop → *Containers*: `sisco` con los 3 contenedores en verde.
2. Terminal: `docker compose ps` (estado *healthy*, puertos) y `docker network inspect sisco-red` (IPs .10/.20/.30).
3. Terminal: `docker exec sisco-bd cat /etc/redhat-release` y `docker exec sisco-bd postgres --version`.
4. Terminal: `docker exec sisco-bd cat /var/lib/pgsql/18/data/pg_hba.conf | tail -3` (regla para 172.20.56.20).
5. Terminal: `docker exec sisco-backend bash -c "cat /etc/os-release | head -2; java -version; /opt/tomcat/bin/version.sh"`.
6. Terminal: `docker exec sisco-backend ls -l /opt/tomcat/webapps` (`sisco.war` y carpeta `sisco`) y `docker compose logs backend | Select-String "Tomcat started","Backend desplegado"`.
7. Terminal: `docker exec sisco-frontend bash -c "cat /etc/almalinux-release; httpd -v; ls /var/www/html"`.
8. Navegador: Swagger en http://localhost:8080/sisco/swagger-ui/index.html.
9. Navegador: login y dashboard con cada rol (ADMIN, GESTOR, SUPERVISOR).
10. Navegador: registro de un dato + terminal con el `select` que lo muestra en la BD.
11. Navegador: reporte exportado en PDF y Excel.

## Solución de problemas

| Síntoma | Causa / solución |
|---|---|
| `preparar.ps1` dice que Docker no está iniciado | Abrir Docker Desktop y esperar *Engine running* |
| `port is already allocated` | Otro programa usa 8080/8081: cerrarlo o cambiar el puerto izquierdo en `docker-compose.yml` (si cambia 8081, actualizar `CORS_ALLOWED_ORIGINS`) |
| Backend `unhealthy` | `docker compose logs backend`: revisar conexión a la BD o variables del `.env` |
| Login falla con error de red en el navegador | El backend aún está desplegando (esperar `healthy`) o el CORS no incluye el origen usado |
| Cambié la contraseña de la BD en `.env` y ya no conecta | La BD se creó con la anterior: `docker compose down -v` y volver a levantar |
