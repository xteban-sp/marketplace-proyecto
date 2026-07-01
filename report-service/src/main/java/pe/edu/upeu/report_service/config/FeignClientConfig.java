package pe.edu.upeu.report_service.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.edu.upeu.report_service.security.ServiceTokenProvider;

/**
 * Reenvia el Authorization entrante a las llamadas Feign; si no hay (ej. desde
 * un consumidor Kafka), adjunta un token de servicio.
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
