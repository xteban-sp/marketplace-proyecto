package pe.edu.upeu.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.payment_service.entity.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findByExternalReference(String externalReference);
}
