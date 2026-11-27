# Despliegue distribuido de SISCO — Escenario 2 (tres máquinas virtuales)

> **Escenario elegido por el equipo: Escenario 4 (Docker)** — ver [`despliegue-docker.md`](despliegue-docker.md).
> Esta guía se conserva como alternativa con máquinas virtuales.

Guía paso a paso para desplegar SISCO en **tres máquinas virtuales de VirtualBox**, cada una con un
único rol e instalada **solo por consola** (sin entorno gráfico).

```
 Equipo anfitrión (Windows)               Red host-only 192.168.56.0/24
 Navegador ──HTTP:80──▶ VM-Frontend ─┐
                        192.168.56.30│  (el navegador descarga el frontend y luego
                        AlmaLinux 9  │   llama directamente a la API del backend)
 Navegador ──HTTP:8080─▶ VM-Backend ─┼──JDBC:5432──▶ VM-BD
                        192.168.56.20│                192.168.56.10
                        Ubuntu 24.04 │                Rocky Linux 9
                        Tomcat 11    │                PostgreSQL 18
```

| VM | Sistema operativo | Software | IP fija | Puerto abierto | RAM | Disco (dinámico) |
|---|---|---|---|---|---|---|
| VM-BD | Rocky Linux 9 (minimal) | PostgreSQL 18 | 192.168.56.10 | 5432 solo para 192.168.56.20 | 1 GB | 10 GB |
| VM-Backend | Ubuntu Server 24.04 LTS | OpenJDK 17 + Apache Tomcat 11 + `sisco.war` | 192.168.56.20 | 8080 | 1.5 GB | 15 GB |
| VM-Frontend | AlmaLinux 9 (minimal) | Apache HTTP Server + build de Angular | 192.168.56.30 | 80 | 768 MB | 10 GB |

Espacio real ocupado tras la instalación: unos 3 GB + 5 GB + 2.5 GB, más unos 6 GB de ISO (se pueden
borrar al terminar). Con 8 GB de RAM en el anfitrión, cierren IntelliJ y Chrome mientras las tres VM
están encendidas.

> **Capturas para el informe (apartado 5.7):** cada captura debe ser de **pantalla completa** del
> equipo anfitrión, con la **barra de tareas visible para que se vean la fecha y la hora**. Usen
> `Impr Pant` o `Win + Impr Pant` (se guardan en *Imágenes > Capturas de pantalla*). No recorten.
> En la sección [Lista de capturas](#9-lista-de-capturas-para-el-informe) está qué capturar en cada paso.

---

## 0. Preparar el entorno en el anfitrión

1. Descargar las ISO:
   - Rocky Linux 9 *Minimal*: https://rockylinux.org/download
   - Ubuntu Server 24.04 LTS: https://ubuntu.com/download/server
   - AlmaLinux 9 *Minimal*: https://almalinux.org/get-almalinux
2. Red host-only en VirtualBox: *Archivo > Herramientas > Administrador de red > Redes solo-anfitrión*.
   Debe existir una red con IP `192.168.56.1/24`. **Desactiven su servidor DHCP**, porque las IP serán fijas.
3. Cada VM lleva **dos adaptadores de red**:
   - Adaptador 1: **NAT**, para descargar paquetes de Internet.
   - Adaptador 2: **Red solo-anfitrión**, para que las VM se vean entre sí y el anfitrión las vea.
4. Durante la instalación elegir instalación mínima o *Server* (sin escritorio), crear un usuario
   administrador (por ejemplo `fadide`) y habilitar SSH.
5. En el anfitrión, compilar los dos proyectos:

```powershell
# Backend (IntelliJ: Maven > Lifecycle > package, o en la terminal):
cd C:\Users\<usuario>\IdeaProjects\FADIDE---Backend
.\mvnw.cmd clean package -DskipTests        # genera target\sisco.war

# Frontend (VS Code, terminal):
cd "...\FADIDE---Frontend"
npx ng build                                 # genera dist\sisco-frontend\browser
```

> `src/environments/environment.ts` ya apunta a `http://192.168.56.20:8080/sisco/api`. Si cambian las
> IP, editen ese archivo **antes** de `ng build` (requisito 4.1: la URL no puede ser `localhost`).

---

## 1. IP fija en cada VM

Ver el nombre de la interfaz del adaptador 2 (normalmente `enp0s8`): `ip -br addr`.

**Rocky Linux y AlmaLinux (NetworkManager):**

```bash
# VM-BD: 192.168.56.10   ·   VM-Frontend: 192.168.56.30
sudo nmcli con add type ethernet ifname enp0s8 con-name hostonly \
     ipv4.method manual ipv4.addresses 192.168.56.10/24
sudo nmcli con up hostonly
sudo hostnamectl set-hostname vm-bd            # vm-frontend en la otra VM
```

**Ubuntu Server (netplan) en VM-Backend:**

```bash
sudo tee /etc/netplan/60-hostonly.yaml > /dev/null <<'EOF'
network:
  version: 2
  ethernets:
    enp0s8:
      dhcp4: false
      addresses: [192.168.56.20/24]
EOF
sudo chmod 600 /etc/netplan/60-hostonly.yaml
sudo netplan apply
sudo hostnamectl set-hostname vm-backend
```

Comprobar desde el anfitrión: `ping 192.168.56.10`, `ping 192.168.56.20`, `ping 192.168.56.30`.

---

## 2. VM-BD — PostgreSQL 18 en Rocky Linux 9

```bash
sudo dnf update -y
# Repositorio oficial de PostgreSQL (la misma versión 18 usada en desarrollo)
sudo dnf install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-9-x86_64/pgdg-redhat-repo-latest.noarch.rpm
sudo dnf -qy module disable postgresql
sudo dnf install -y postgresql18-server
sudo /usr/pgsql-18/bin/postgresql-18-setup initdb
sudo systemctl enable --now postgresql-18

# Usuario y base de datos de la aplicación (usen una contraseña propia)
sudo -u postgres psql -c "CREATE USER sisco WITH PASSWORD 'CAMBIAR_password_de_la_bd';"
sudo -u postgres psql -c "CREATE DATABASE sisco_db OWNER sisco;"
```

**Aceptar conexiones solo desde el backend (requisito 4.5):**

```bash
# 1) Escuchar también en la IP de la red host-only
sudo sed -i "s/^#listen_addresses = 'localhost'/listen_addresses = 'localhost,192.168.56.10'/" \
     /var/lib/pgsql/18/data/postgresql.conf

# 2) Autenticación por dirección: solo VM-Backend, solo la base sisco_db, solo el usuario sisco
echo "host  sisco_db  sisco  192.168.56.20/32  scram-sha-256" | \
     sudo tee -a /var/lib/pgsql/18/data/pg_hba.conf

sudo systemctl restart postgresql-18

# 3) Firewall: abrir 5432 únicamente para la IP del backend
sudo firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="192.168.56.20/32" port port="5432" protocol="tcp" accept'
sudo firewall-cmd --reload

# Verificación
sudo ss -tlnp | grep 5432
sudo firewall-cmd --list-rich-rules
```

---

## 3. VM-Backend — OpenJDK 17 y Apache Tomcat 11 en Ubuntu Server

```bash
sudo apt update && sudo apt upgrade -y
# fontconfig: lo necesita Apache POI para dimensionar las columnas de los Excel en un servidor sin escritorio
sudo apt install -y openjdk-17-jre-headless fontconfig postgresql-client curl
java -version

# Comprobar que la base de datos responde desde el backend
psql -h 192.168.56.10 -U sisco -d sisco_db -c "select 1;"
```

**Tomcat 11 (versión portable, se descomprime y se levanta con `bin/startup.sh`):**

```bash
# Última versión 11.0.x publicada
TOMCAT_VER=$(curl -s https://dlcdn.apache.org/tomcat/tomcat-11/ | grep -o 'v11\.[0-9]*\.[0-9]*' | sort -V | tail -1 | tr -d v)
echo "Tomcat $TOMCAT_VER"
curl -O "https://dlcdn.apache.org/tomcat/tomcat-11/v$TOMCAT_VER/bin/apache-tomcat-$TOMCAT_VER.tar.gz"

sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat
sudo tar xzf "apache-tomcat-$TOMCAT_VER.tar.gz" -C /opt/tomcat --strip-components=1
sudo rm -rf /opt/tomcat/webapps/{ROOT,docs,examples,host-manager,manager}   # sin apps de ejemplo
sudo chown -R tomcat:tomcat /opt/tomcat
sudo chmod +x /opt/tomcat/bin/*.sh
```

**Variables de entorno (requisito 4.6: las credenciales viven en el servidor, no en el repositorio):**

```bash
# Copiar la plantilla despliegue/tomcat/setenv.sh.example desde el anfitrión:
#   scp despliegue\tomcat\setenv.sh.example fadide@192.168.56.20:/tmp/setenv.sh
sudo mv /tmp/setenv.sh /opt/tomcat/bin/setenv.sh
sudo nano /opt/tomcat/bin/setenv.sh          # reemplazar los valores CAMBIAR_...
openssl rand -base64 48                       # sirve como JWT_SECRET
sudo chown tomcat:tomcat /opt/tomcat/bin/setenv.sh
sudo chmod 750 /opt/tomcat/bin/setenv.sh
```

**Publicar el WAR (requisito 4.3: sin IDE, en Tomcat 11):**

```bash
# Desde el anfitrión (PowerShell):
#   scp target\sisco.war fadide@192.168.56.20:/tmp/
sudo mv /tmp/sisco.war /opt/tomcat/webapps/
sudo chown tomcat:tomcat /opt/tomcat/webapps/sisco.war

sudo ufw allow OpenSSH
sudo ufw allow 8080/tcp
sudo ufw enable

sudo -u tomcat /opt/tomcat/bin/startup.sh
tail -f /opt/tomcat/logs/catalina.out        # esperar "Started SiscoBackendApplication"; salir con Ctrl+C
```

Verificación:

```bash
curl -i http://192.168.56.20:8080/sisco/api/auth/me     # 401 en JSON = el backend responde y exige token
```

- Swagger UI: http://192.168.56.20:8080/sisco/swagger-ui/index.html
- Para detenerlo: `sudo -u tomcat /opt/tomcat/bin/shutdown.sh`.
- Al primer arranque Hibernate crea las tablas y `DataSeeder` registra los roles y el administrador.

**Datos de demostración** (una sola vez, después del primer arranque):

```bash
# scp despliegue\postgresql\datos-demo.sql fadide@192.168.56.20:/tmp/
psql -h 192.168.56.10 -U sisco -d sisco_db -f /tmp/datos-demo.sql
```

Crea 5 clientes, 6 obras, 7 contratos, pagos de los últimos 6 meses, penalidades, supervisiones y
los usuarios `gestor@fadide.com` (Gestor123.), `supervisor@fadide.com` y `supervisor2@fadide.com`
(Supervisor123.).

<details>
<summary>Opcional: arrancar Tomcat como servicio al encender la VM</summary>

```bash
sudo tee /etc/systemd/system/tomcat.service > /dev/null <<'EOF'
[Unit]
Description=Apache Tomcat 11 - SISCO
After=network.target

[Service]
Type=forking
User=tomcat
Group=tomcat
ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF
sudo systemctl daemon-reload
sudo systemctl enable --now tomcat
```
</details>

---

## 4. VM-Frontend — Apache HTTP Server en AlmaLinux 9

```bash
sudo dnf update -y
sudo dnf install -y httpd
sudo systemctl enable --now httpd
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --reload
```

Copiar el build de producción (requisito 4.2):

```powershell
# Desde el anfitrión (PowerShell), carpeta del frontend:
scp -r dist\sisco-frontend\browser fadide@192.168.56.30:/tmp/sisco
scp ..\FADIDE---Backend\despliegue\apache\sisco.conf fadide@192.168.56.30:/tmp/
```

```bash
sudo rm -rf /var/www/html/*
sudo cp -r /tmp/sisco/. /var/www/html/          # el "." copia también el .htaccess oculto
sudo restorecon -Rv /var/www/html               # contexto SELinux correcto para httpd
sudo cp /tmp/sisco.conf /etc/httpd/conf.d/sisco.conf
sudo apachectl configtest                       # debe decir "Syntax OK"
sudo systemctl restart httpd
ls -la /var/www/html                            # debe aparecer .htaccess
```

El `.htaccess` del build redirige a `index.html` cualquier ruta que no sea un archivo, así que
recargar `/pages/cliente` no da 404.

---

## 5. Prueba de punta a punta (desde el anfitrión o desde otra PC de la red)

1. Abrir **http://192.168.56.30** → portada de SISCO.
2. Iniciar sesión como `admin@fadide.com` → dashboard con indicadores y gráficos (requisito 4.7).
3. `F12 > Red`: las peticiones van a `http://192.168.56.20:8080/sisco/api/...` y llevan la cabecera
   `Authorization: Bearer ...` (propagación del token).
4. Entrar como `supervisor@fadide.com` e intentar abrir `/pages/usuario` → acceso denegado (restricción por rol).
5. Registrar un cliente nuevo desde la aplicación (requisito 4.8) y comprobarlo directamente en la
   base de datos (requisito 4.9):

```bash
# En VM-BD
sudo -u postgres psql -d sisco_db -c "select id_cliente, razon_social, ruc from clientes order by id_cliente desc limit 3;"
```

6. Recargar con F5 en `/pages/contrato` → la pantalla carga (no 404).
7. Reportes → descargar un PDF y un Excel.

---

## 6. Cumplimiento de los requisitos técnicos

| Requisito | Cómo se cumple |
|---|---|
| 4.1 URL de la API no es localhost | `environment.ts` → `http://192.168.56.20:8080/sisco/api`, se compila con `ng build` |
| 4.2 Frontend compilado y rutas al recargar | `dist/sisco-frontend/browser` en Apache + `.htaccess` (mod_rewrite) + `AllowOverride All` |
| 4.3 WAR en Tomcat 11 | `packaging war`, `ServletInitializer`, `target/sisco.war` en `/opt/tomcat/webapps` |
| 4.4 CORS con el origen real | `CORS_ALLOWED_ORIGINS=http://192.168.56.30` en `setenv.sh` |
| 4.5 BD acepta solo al backend | `listen_addresses`, `pg_hba.conf` con `192.168.56.20/32` y firewalld con regla por IP |
| 4.6 Variables de entorno | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS` en `setenv.sh` |
| 4.7 JWT de punta a punta | login → token → cabecera `Authorization` → `@PreAuthorize` por rol |
| 4.8 Módulos con datos reales | clientes, obras, contratos, pagos, penalidades, supervisión, reportes |
| 4.9 Persistencia verificable | consulta `psql` en VM-BD del registro creado desde la interfaz |

---

## 7. Problemas frecuentes

| Síntoma | Causa probable y solución |
|---|---|
| La pantalla de login dice "No se pudo conectar con el servidor" | Tomcat no está levantado, el puerto 8080 está cerrado (`sudo ufw status`) o `environment.ts` tiene otra IP. |
| Error de CORS en la consola del navegador | `CORS_ALLOWED_ORIGINS` no coincide exactamente con `http://192.168.56.30` (sin `/` final). Reiniciar Tomcat tras cambiarlo. |
| `catalina.out`: *Connection refused* o *no pg_hba.conf entry* | Revisar `listen_addresses`, la línea de `pg_hba.conf` y la regla de firewalld en VM-BD. |
| 404 al recargar una ruta del frontend | Falta el `.htaccess` en `/var/www/html` o `AllowOverride All` en `sisco.conf`. |
| 403 *Forbidden* de Apache | Contexto SELinux: ejecutar `sudo restorecon -Rv /var/www/html`. |
| El Excel no se descarga y hay un error de fuentes en `catalina.out` | Instalar `fontconfig` y confirmar `-Djava.awt.headless=true` en `setenv.sh`. |
| Las IP cambiaron al reiniciar | Revisar que el DHCP de la red host-only esté desactivado y la IP fija configurada. |

---

## 8. Infraestructura sostenible y eficiente (ODS 9) — insumos para el informe

- **Consolidación con virtualización:** tres servidores lógicos en un solo equipo físico, sin hardware adicional.
- **Instalación mínima por consola:** sin entorno gráfico, cada VM usa entre 0.75 y 1.5 GB de RAM y
  2.5 a 5 GB de disco, frente a los 4 GB o más que exige una instalación con escritorio.
- **Discos dinámicos:** solo ocupan el espacio realmente usado.
- **Separación por capas:** cada servidor puede escalarse o reemplazarse por separado (por ejemplo,
  llevar solo la base de datos a un servicio administrado) sin rehacer todo el sistema.
- **Seguridad por mínimo privilegio:** solo se abren los puertos necesarios y la base de datos acepta
  únicamente al backend, lo que reduce la superficie de ataque.
- **Configuración externalizada:** el mismo WAR sirve para cualquier ambiente cambiando solo las variables de entorno.
- **Menos papel:** contratos, pagos y supervisiones dejan de registrarse en documentos físicos.

---

## 9. Lista de capturas para el informe

Todas de **pantalla completa** con la fecha y la hora del sistema visibles:

1. VirtualBox con las tres VM encendidas y su configuración de red (NAT + solo-anfitrión).
2. `ip -br addr` en cada VM mostrando su IP fija.
3. VM-BD: `systemctl status postgresql-18`, la línea de `pg_hba.conf` y `firewall-cmd --list-rich-rules`.
4. VM-Backend: `java -version`, `setenv.sh` (**tapen las contraseñas**) y `catalina.out` con "Started SiscoBackendApplication".
5. VM-Backend: `curl -i http://192.168.56.20:8080/sisco/api/auth/me` devolviendo 401.
6. VM-Frontend: `systemctl status httpd`, `ls -la /var/www/html` (con `.htaccess`) y `apachectl configtest`.
7. Navegador del anfitrión en `http://192.168.56.30`: portada, login y dashboard.
8. Herramientas de desarrollo (F12 > Red) con la petición al backend y la cabecera `Authorization`.
9. Acceso denegado del SUPERVISOR al intentar abrir Usuarios.
10. Cliente registrado desde la interfaz y la consulta `psql` en VM-BD que lo muestra.
11. Reporte exportado a PDF y a Excel.

---

## Anexo: ensayo local con Tomcat portable en Windows (Escenario 1)

Sirve para validar el WAR antes de pasarlo a las VM:

1. Descargar el **zip** de Tomcat 11 (*Core > zip*) desde https://tomcat.apache.org/download-11.cgi y descomprimirlo.
2. Copiar `despliegue\tomcat\setenv.bat.example` como `apache-tomcat-11.x\bin\setenv.bat` y completar los valores.
3. Copiar `target\sisco.war` a `apache-tomcat-11.x\webapps\`.
4. Entrar a `bin` y ejecutar `startup.bat`.
5. Probar http://localhost:8080/sisco/swagger-ui/index.html.
   Si `ng serve` usa `environment.development.ts` (que apunta a `localhost:8080/api`), cambien
   temporalmente esa URL a `http://localhost:8080/sisco/api`.
