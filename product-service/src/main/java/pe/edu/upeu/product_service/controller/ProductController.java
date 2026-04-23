package pe.edu.upeu.product_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.product_service.dto.request.ProductRequestDTO;
import pe.edu.upeu.product_service.dto.response.ProductResponseDTO;
import pe.edu.upeu.product_service.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Gestión de productos del Marketplace Universitario")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Crear producto", description = "Publica un nuevo producto en el marketplace")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
    }

    @Operation(summary = "Obtener producto por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @Operation(summary = "Listar todos los productos activos", description = "Retorna lista paginada de productos activos")
    @ApiResponse(responseCode = "200", description = "Lista paginada de productos")
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAll(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(productService.getAll(pageable));
    }

    @Operation(
            summary = "Buscar productos",
            description = "Búsqueda paginada por nombre y/o categoría. Ambos parámetros son opcionales."
    )
    @ApiResponse(responseCode = "200", description = "Resultados de búsqueda paginados")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> search(
            @Parameter(description = "Nombre del producto (parcial, ignora mayúsculas)")
            @RequestParam(required = false) String name,
            @Parameter(description = "ID de la categoría")
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(productService.search(name, categoryId, pageable));
    }

    @Operation(summary = "Listar productos por vendedor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Productos del vendedor"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado")
    })
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ProductResponseDTO>> getBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(productService.getBySeller(sellerId));
    }

    @Operation(summary = "Listar productos por categoría")
    @ApiResponse(responseCode = "200", description = "Productos de la categoría")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getByCategory(categoryId));
    }

    @Operation(summary = "Actualizar producto", description = "Modifica los datos de un producto existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto o categoría no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @Operation(summary = "Eliminar producto", description = "Soft delete — el producto se desactiva, no se borra físicamente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
