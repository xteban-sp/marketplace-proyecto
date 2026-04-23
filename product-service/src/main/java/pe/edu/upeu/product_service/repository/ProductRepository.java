package pe.edu.upeu.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.product_service.entity.Product;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoriaId(Long categoriaId);
    List<Product> findByTituloContainingIgnoreCase(String consulta);
}
