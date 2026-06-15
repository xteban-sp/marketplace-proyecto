package pe.edu.upeu.auth_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envío de correos de verificación. Si no hay credenciales configuradas
 * (MAIL_USERNAME vacío), queda DESHABILITADO y el registro funciona sin
 * verificación, para no bloquear el desarrollo.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean isEnabled() {
        return from != null && !from.isBlank();
    }

    public void sendVerification(String to, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Activa tu cuenta en Feria");
        message.setText(
                "Bienvenido a Feria, el marketplace universitario.\n\n" +
                "Para activar tu cuenta haz clic en el siguiente enlace:\n" + link + "\n\n" +
                "Si no creaste esta cuenta, ignora este correo."
        );
        mailSender.send(message);
        log.info("Correo de verificación enviado a {}", to);
    }
}
