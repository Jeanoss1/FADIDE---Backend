package pe.com.fadide.sisco.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Documentación Swagger UI en /swagger-ui.html con botón "Authorize" para el token JWT
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI siscoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SISCO API")
                        .description("API del sistema de supervisión de contratos de obra - FADIDE")
                        .version("0.0.1"))
                // URL relativa: funciona igual en localhost y detrás del túnel HTTPS
                .servers(List.of(new Server().url("/")))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
