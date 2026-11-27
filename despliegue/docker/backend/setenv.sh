#!/bin/sh
# Apache Tomcat lee este archivo al ejecutar bin/startup.sh.
# DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, CORS_ALLOWED_ORIGINS, ADMIN_CORREO y
# ADMIN_PASSWORD ya vienen como variables de entorno del contenedor (archivo .env).
# Aquí solo se ajusta la JVM: memoria acotada, modo sin interfaz gráfica (necesario para
# generar PDF y Excel en un servidor sin escritorio) y zona horaria de Perú.
export CATALINA_OPTS="$CATALINA_OPTS -Xms256m -Xmx512m -Djava.awt.headless=true -Duser.timezone=America/Lima -Dfile.encoding=UTF-8"
