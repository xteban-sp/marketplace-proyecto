package pe.edu.upeu.report_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthClient {
    // Devuelve datos basicos del usuario (incluye "id").
    @GetMapping("/api/auth/users/{username}")
    Map<String, Object> getUserByUsername(@PathVariable("username") String username);
}
