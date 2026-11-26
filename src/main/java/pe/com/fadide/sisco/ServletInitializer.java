package pe.com.fadide.sisco;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Punto de entrada cuando el backend se despliega como WAR en un Apache Tomcat 11 externo.
 * Tomcat no ejecuta el método main: llama a configure() al publicar sisco.war.
 * En desarrollo se sigue usando SiscoBackendApplication.main() con el Tomcat embebido.
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SiscoBackendApplication.class);
    }
}
