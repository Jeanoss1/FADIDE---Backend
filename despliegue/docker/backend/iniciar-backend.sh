#!/bin/bash
# Arranca Tomcat con su script portable (bin/startup.sh), espera a que sisco.war quede
# desplegado y carga los datos de demostración (idempotente). Luego mantiene el contenedor
# vivo mostrando catalina.out; al detener el contenedor se ejecuta bin/shutdown.sh.
set -e
cd /opt/tomcat

detener() {
    echo ">> Deteniendo Tomcat (shutdown.sh)"
    bin/shutdown.sh 30 -force || true
    exit 0
}
trap detener SIGTERM SIGINT

touch logs/catalina.out
bin/startup.sh

echo ">> Esperando que /sisco responda..."
for i in $(seq 1 90); do
    codigo=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/sisco/api/dashboard || true)
    if [ "$codigo" = "401" ] || [ "$codigo" = "403" ]; then
        echo ">> Backend desplegado en http://localhost:8080/sisco"
        if [ "${CARGAR_DATOS_DEMO:-true}" = "true" ]; then
            echo ">> Cargando datos de demostración en ${DB_HOST}"
            PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -U "$DB_USERNAME" -d sisco_db \
                -v ON_ERROR_STOP=1 -f datos-demo.sql || echo "!! No se pudieron cargar los datos de demostración"
        fi
        break
    fi
    sleep 2
done

tail -n +1 -F logs/catalina.out &
wait $!
