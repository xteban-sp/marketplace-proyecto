package pe.edu.upeu.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.product_service.entity.Category;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Buscar categoría por nombre exacto
    Optional<Category> findByName(String name);

    // Verificar si ya existe una categoría con ese nombre (para validar duplicados)
    boolean existsByName(String name);
}