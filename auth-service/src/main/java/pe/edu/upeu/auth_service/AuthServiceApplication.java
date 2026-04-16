package pe.edu.upeu.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    public static void generarClaves() {
        System.out.println("\n=== CLAVES PARA DESARROLLO ===\n");

        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String secret = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("JWT_SECRET: " + secret);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("\nPasswords hash:");
        System.out.println("admin123: " + encoder.encode("admin123"));
        System.out.println("user123: " + encoder.encode("user123"));
        System.out.println("123456: " + encoder.encode("123456"));

        System.out.println("\n==============================\n");
    }
}