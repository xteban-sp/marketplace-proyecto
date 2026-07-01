package pe.edu.upeu.report_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.report_service.entity.ReviewRecord;

import java.util.List;

public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {

    boolean existsByResenaId(String resenaId);

    @Query("select coalesce(avg(r.puntuacion),0) from ReviewRecord r")
    Double promedioGlobal();

    // Distribución de estrellas.
    @Query(value = """
        SELECT puntuacion, COUNT(*) AS cantidad
        FROM review_record
        GROUP BY puntuacion ORDER BY puntuacion
        """, nativeQuery = true)
    List<Object[]> distribucion();

    // Productos más populares por número de reseñas.
    @Query(value = """
        SELECT producto_id,
               COUNT(*) AS resenas,
               ROUND(AVG(puntuacion), 2) AS promedio
        FROM review_record
        GROUP BY producto_id
        ORDER BY resenas DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> topProductos(@Param("limite") int limite);
}
