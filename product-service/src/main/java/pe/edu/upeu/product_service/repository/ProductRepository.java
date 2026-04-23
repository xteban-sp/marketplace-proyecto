package pe.edu.upeu.product_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.product_service.entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Buscar todos los productos activos de un vendedor
    List<Product> findBySellerIdAndActiveTrue(Long sellerId);

    // Buscar todos los productos activos de una categoría
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    // Buscar productos activos por nombre (contiene, ignora mayúsculas)
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    // Búsqueda combinada: nombre + categoría (paginada)
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
            AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:categoryId IS NULL OR p.category.id = :categoryId)
            """)
    Page<Product> searchProducts(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // Todos los productos activos (paginado)
    Page<Product> findByActiveTrue(Pageable pageable);
}
