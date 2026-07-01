package pe.edu.upeu.report_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_record", indexes = {
        @Index(name = "idx_review_resena", columnList = "resenaId", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String resenaId;
    private Long productoId;
    private String usuarioId;
    private Integer puntuacion;
    private LocalDateTime fecha;
}
