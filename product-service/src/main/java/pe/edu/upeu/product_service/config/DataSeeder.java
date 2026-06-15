package pe.edu.upeu.product_service.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.edu.upeu.product_service.entity.Category;
import pe.edu.upeu.product_service.entity.Product;
import pe.edu.upeu.product_service.repository.CategoryRepository;
import pe.edu.upeu.product_service.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Carga datos de ejemplo (categorias + productos) la primera vez, para que el
 * frontend tenga catalogo con que trabajar. Solo corre si no hay categorias.
 *
 * Nota: el sellerId es un UUID de demostracion; en un flujo real corresponde al
 * UUID de un usuario con rol SELLER en auth-service.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Value("${seed.enabled:true}")
    private boolean enabled;

    @Value("${seed.seller-id:11111111-1111-1111-1111-111111111111}")
    private String seedSellerId;

    @Override
    public void run(String... args) {
        if (!enabled || categoryRepository.count() > 0) {
            return;
        }

        UUID sellerId = UUID.fromString(seedSellerId);

        Category electronica = categoryRepository.save(
                Category.builder().name("Electronica").description("Dispositivos y accesorios").build());
        Category libros = categoryRepository.save(
                Category.builder().name("Libros").description("Libros y material de estudio").build());
        Category ropa = categoryRepository.save(
                Category.builder().name("Ropa").description("Vestimenta universitaria").build());

        productRepository.saveAll(List.of(
                Product.builder().name("Audifonos Bluetooth").description("Audifonos inalambricos")
                        .price(new BigDecimal("89.90")).stock(25).imageUrl(null).active(true)
                        .sellerId(sellerId).category(electronica).build(),
                Product.builder().name("Calculadora cientifica").description("Ideal para ingenieria")
                        .price(new BigDecimal("45.00")).stock(40).imageUrl(null).active(true)
                        .sellerId(sellerId).category(electronica).build(),
                Product.builder().name("Libro de Algoritmos").description("Estructuras de datos y algoritmos")
                        .price(new BigDecimal("120.00")).stock(15).imageUrl(null).active(true)
                        .sellerId(sellerId).category(libros).build(),
                Product.builder().name("Polo universitario").description("Talla M, algodon")
                        .price(new BigDecimal("39.90")).stock(60).imageUrl(null).active(true)
                        .sellerId(sellerId).category(ropa).build()
        ));

        log.info("DataSeeder: catalogo de ejemplo creado (3 categorias, 4 productos).");
    }
}
