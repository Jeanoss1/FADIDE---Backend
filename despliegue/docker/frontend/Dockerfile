# =====================================================================================
# Servidor Web · AlmaLinux 9 + Apache HTTP Server (httpd)
# Equivale a la VM-Frontend: httpd se instala con dnf y publica el build de producción
# de Angular; el .htaccess del build redirige las rutas internas a index.html.
# =====================================================================================
FROM almalinux:9

ENV TZ=America/Lima
RUN dnf install -y httpd \
 && dnf clean all \
 && echo 'ServerName localhost' >> /etc/httpd/conf/httpd.conf \
 && echo 'ServerTokens Prod' >> /etc/httpd/conf/httpd.conf \
 && echo 'ServerSignature Off' >> /etc/httpd/conf/httpd.conf \
 && rm -f /etc/httpd/conf.d/welcome.conf

COPY sisco.conf /etc/httpd/conf.d/sisco.conf
COPY dist/ /var/www/html/

EXPOSE 80
HEALTHCHECK --interval=10s --timeout=5s --retries=5 CMD curl -fs http://localhost/ -o /dev/null || exit 1
CMD ["httpd", "-DFOREGROUND"]
