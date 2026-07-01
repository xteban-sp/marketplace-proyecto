package pe.edu.upeu.report_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_record", indexes = {
        @Index(name = "idx_order_pedido", columnList = "pedidoId", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String pedidoId;
    private String compradorId;
    private String vendedorId;
    private BigDecimal total;
    private LocalDateTime fecha;
}
