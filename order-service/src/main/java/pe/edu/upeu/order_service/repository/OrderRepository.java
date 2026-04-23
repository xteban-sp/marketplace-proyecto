package pe.edu.upeu.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.order_service.entity.Order;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCompradorId(UUID compradorId);
    List<Order> findByVendedorId(UUID vendedorId);
}
