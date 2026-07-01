package pe.edu.upeu.report_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.report_service.entity.OrderRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRecordRepository extends JpaRepository<OrderRecord, Long> {

    boolean existsByPedidoId(String pedidoId);

    @Query(value = """
        SELECT to_char(date_trunc('day', fecha), 'YYYY-MM-DD') AS dia,
               COUNT(*) AS pedidos,
               COALESCE(SUM(total), 0) AS monto
        FROM order_record
        WHERE fecha >= :desde
        GROUP BY 1 ORDER BY 1
        """, nativeQuery = true)
    List<Object[]> pedidosPorDia(@Param("desde") LocalDateTime desde);
}
