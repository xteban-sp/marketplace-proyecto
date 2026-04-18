package pe.edu.upeu.auth_service.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<String> home() {
        String html = "<!DOCTYPE html><html><head><title>Auth Service</title>" +
                "<style>body{font-family:sans-serif;max-width:800px;margin:50px auto;padding:20px;line-height:1.6}" +
                "h1{color:#22c55e}code{background:#f3f4f6;padding:2px 6px;border-radius:4px}" +
                ".endpoint{background:#e0e7ff;padding:12px;border-radius:6px;margin:10px 0}</style></head><body>" +
                "<h1>✅ Auth Service - Marketplace</h1>" +
                "<p>Microservicio de autenticación funcionando correctamente.</p>" +
                "<h3>📡 Endpoints:</h3>" +
                "<div class='endpoint'><strong>POST</strong> <code>/auth/register</code><br>" +
                "Registrar nuevo usuario<br>Body: <code>{\"username\":\"...\",\"password\":\"...\"}</code></div>" +
                "<div class='endpoint'><strong>POST</strong> <code>/auth/login</code><br>" +
                "Iniciar sesión y obtener JWT<br>Body: <code>{\"username\":\"...\",\"password\":\"...\"}</code></div>" +
                "<p><em>💡 Usa Postman, curl o Thunder Client para probar los endpoints POST.</em></p>" +
                "</body></html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}