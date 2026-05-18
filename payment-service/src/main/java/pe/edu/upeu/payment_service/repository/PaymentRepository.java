package pe.edu.upeu.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.payment_service.entity.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pe.edu.upeu.payment_service.entity.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByPedidoId(UUID pedidoId);
    Optional<Payment> findByReferenciaExterna(String referenciaExterna);
    boolean existsByPedidoIdAndEstadoIn(UUID pedidoId, List<PaymentStatus> estados);
}
