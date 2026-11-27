# =====================================================================================
# Servidor de Aplicaciones · Ubuntu 24.04 + OpenJDK 17 + Apache Tomcat 11 (portable)
# Equivale a la VM-Backend: Java se instala con apt, Tomcat se descarga como .tar.gz
# portable en /opt/tomcat y sisco.war se despliega en webapps (contexto /sisco).
# =====================================================================================
FROM ubuntu:24.04

ARG TOMCAT_VERSION=11.0.29
ENV DEBIAN_FRONTEND=noninteractive \
    CATALINA_HOME=/opt/tomcat \
    TZ=America/Lima

# Java 17 (sin interfaz gráfica), fuentes para PDF/Excel y cliente psql para cargar datos
RUN apt-get update \
 && apt-get install -y --no-install-recommends openjdk-17-jre-headless fontconfig fonts-dejavu-core \
        curl ca-certificates postgresql-client tzdata \
 && rm -rf /var/lib/apt/lists/*

# Apache Tomcat 11 portable (dlcdn y, si ya no está esa versión, el archivo histórico)
RUN useradd -r -m -d /opt/tomcat -s /bin/false tomcat \
 && cd /tmp \
 && ( curl -fsSLO https://dlcdn.apache.org/tomcat/tomcat-11/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz \
   || curl -fsSLO https://archive.apache.org/dist/tomcat/tomcat-11/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz ) \
 && tar xzf apache-tomcat-${TOMCAT_VERSION}.tar.gz -C /opt/tomcat --strip-components=1 \
 && rm apache-tomcat-${TOMCAT_VERSION}.tar.gz \
 && rm -rf /opt/tomcat/webapps/* \
 && chown -R tomcat:tomcat /opt/tomcat \
 && chmod +x /opt/tomcat/bin/*.sh

# setenv.sh: memoria de la JVM y modo headless. Las credenciales llegan como variables de
# entorno desde docker-compose (.env), nunca dentro de la imagen.
COPY --chown=tomcat:tomcat --chmod=750 setenv.sh /opt/tomcat/bin/setenv.sh
COPY --chown=tomcat:tomcat sisco.war /opt/tomcat/webapps/sisco.war
COPY --chown=tomcat:tomcat datos-demo.sql /opt/tomcat/datos-demo.sql
COPY --chmod=755 iniciar-backend.sh /usr/local/bin/iniciar-backend.sh

USER tomcat
WORKDIR /opt/tomcat
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=10 \
  CMD curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/sisco/api/dashboard | grep -qE '^(401|403)$' || exit 1
CMD ["/usr/local/bin/iniciar-backend.sh"]
