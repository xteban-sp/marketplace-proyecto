package pe.edu.upeu.report_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_record", indexes = {
        @Index(name = "idx_sales_pago", columnList = "pagoId", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String pagoId;
    private String pedidoId;
    private String compradorId;
    private String vendedorId;
    private BigDecimal monto;
    private String estado;          // APPROVED / FAILED
    private LocalDateTime fecha;
}
