package pe.edu.upeu.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.product_service.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);   // ← requerido por CategoryServiceImpl

    boolean existsByNameAndIdNot(String name, Long id);  // útil para update sin falso positivo
}