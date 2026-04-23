package pe.edu.upeu.product_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .description("Microservicio de gestión de productos del Marketplace Universitario")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jeanpierre")
                                .email("jeanpierre@upeu.edu.pe"))
                        .license(new License()
                                .name("UPEU - Uso Interno")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Servidor local de desarrollo")
                ));
    }
}