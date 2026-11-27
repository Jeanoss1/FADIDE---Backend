#!/bin/bash
# Primer arranque: inicializa el clúster, crea el usuario y la base del sistema y restringe
# el acceso de red solo al servidor de aplicaciones. Arranques siguientes: solo inicia.
set -e

if [ ! -s "$PGDATA/PG_VERSION" ]; then
    echo ">> Inicializando PostgreSQL 18 en $PGDATA"
    initdb -D "$PGDATA" --encoding=UTF8 --locale=es_PE.UTF-8 --auth-local=peer --auth-host=scram-sha-256

    # Escuchar en la red interna (equivale a listen_addresses en postgresql.conf)
    cat >> "$PGDATA/postgresql.conf" <<EOF
listen_addresses = '*'
password_encryption = 'scram-sha-256'
timezone = 'America/Lima'
EOF

    # Solo el servidor de aplicaciones (${BACKEND_IP}) puede conectarse a sisco_db
    cat >> "$PGDATA/pg_hba.conf" <<EOF
host    sisco_db    ${DB_USERNAME}    ${BACKEND_IP}/32    scram-sha-256
EOF

    pg_ctl -D "$PGDATA" -o "-c listen_addresses=''" -w start
    psql -v ON_ERROR_STOP=1 --username postgres <<EOSQL
CREATE ROLE ${DB_USERNAME} LOGIN PASSWORD '${DB_PASSWORD}';
CREATE DATABASE sisco_db OWNER ${DB_USERNAME} ENCODING 'UTF8';
EOSQL
    pg_ctl -D "$PGDATA" -m fast -w stop
    echo ">> Base sisco_db y usuario ${DB_USERNAME} creados"
fi

exec postgres -D "$PGDATA"
