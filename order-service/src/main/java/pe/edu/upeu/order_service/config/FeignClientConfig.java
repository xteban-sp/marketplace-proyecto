package pe.edu.upeu.order_service.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.edu.upeu.order_service.security.ServiceTokenProvider;

/**
 * Interceptor global de Feign: reenvia el header Authorization de la peticion
 * entrante a las llamadas salientes entre microservicios. Si no hay token de
 * usuario (ej. flujos internos), adjunta un token de servicio.
 */
@Configuration
public class FeignClientConfig {

    private final ServiceTokenProvider serviceTokenProvider;

    public FeignClientConfig(ServiceTokenProvider serviceTokenProvider) {
        this.serviceTokenProvider = serviceTokenProvider;
    }

    @Bean
    public RequestInterceptor jwtForwardingInterceptor() {
        return template -> {
            String authHeader = null;
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                authHeader = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            }
            if (authHeader == null || authHeader.isBlank()) {
                authHeader = "Bearer " + serviceTokenProvider.generateServiceToken();
            }
            template.header(HttpHeaders.AUTHORIZATION, authHeader);
        };
    }
}
