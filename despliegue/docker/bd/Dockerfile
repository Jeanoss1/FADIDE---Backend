# =====================================================================================
# Servidor de Base de Datos · Rocky Linux 9 (familia Red Hat) + PostgreSQL 18
# Equivale a la VM-BD: el motor se instala con dnf desde el repositorio oficial PGDG,
# igual que se haría por terminal en un servidor Red Hat.
# =====================================================================================
FROM rockylinux:9

# Repositorio oficial de PostgreSQL y desactivación del módulo de AppStream
RUN dnf install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-9-x86_64/pgdg-redhat-repo-latest.noarch.rpm \
 && dnf -qy module disable postgresql \
 && dnf install -y postgresql18-server glibc-langpack-es \
 && dnf clean all

ENV PGDATA=/var/lib/pgsql/18/data \
    PATH=/usr/pgsql-18/bin:$PATH \
    LANG=es_PE.UTF-8 \
    TZ=America/Lima

COPY --chmod=755 iniciar-bd.sh /usr/local/bin/iniciar-bd.sh

VOLUME /var/lib/pgsql/18/data
EXPOSE 5432
USER postgres
HEALTHCHECK --interval=10s --timeout=5s --retries=10 CMD pg_isready -U postgres -d sisco_db || exit 1
CMD ["/usr/local/bin/iniciar-bd.sh"]
