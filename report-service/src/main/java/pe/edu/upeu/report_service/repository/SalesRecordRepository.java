package pe.edu.upeu.report_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.report_service.entity.SalesRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesRecordRepository extends JpaRepository<SalesRecord, Long> {

    boolean existsByPagoId(String pagoId);

    long countByEstado(String estado);

    @Query("select coalesce(sum(s.monto),0) from SalesRecord s where s.estado = 'APPROVED'")
    java.math.BigDecimal totalIngresos();

    // Ventas por día (últimos :desde). Nativa (Postgres) para truncar la fecha.
    @Query(value = """
        SELECT to_char(date_trunc('day', fecha), 'YYYY-MM-DD') AS dia,
               COALESCE(SUM(monto), 0) AS ingresos,
               COUNT(*) AS ventas
        FROM sales_record
        WHERE estado = 'APPROVED' AND fecha >= :desde
        GROUP BY 1 ORDER BY 1
        """, nativeQuery = true)
    List<Object[]> ventasPorDia(@Param("desde") LocalDateTime desde);

    // Top vendedores por ingresos.
    @Query(value = """
        SELECT vendedor_id,
               COALESCE(SUM(monto), 0) AS ingresos,
               COUNT(*) AS ventas
        FROM sales_record
        WHERE estado = 'APPROVED' AND vendedor_id IS NOT NULL
        GROUP BY vendedor_id
        ORDER BY ingresos DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> topVendedores(@Param("limite") int limite);
}
