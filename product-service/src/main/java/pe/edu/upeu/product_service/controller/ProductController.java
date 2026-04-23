package pe.edu.upeu.product_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.product_service.client.UserClient;
import pe.edu.upeu.product_service.dto.request.ProductRequestDTO;
import pe.edu.upeu.product_service.dto.response.ProductResponseDTO;
import pe.edu.upeu.product_service.service.ProductService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductController {

    private final ProductService productService;
    private final UserClient userClient;

    public ProductController(ProductService productService, UserClient userClient) {
        this.productService = productService;
        this.userClient = userClient;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO create(@Valid @RequestBody ProductRequestDTO request,
                                     @RequestHeader("Authorization") String token) {
        String username = validarRolVendedor(token);
        request.setUsuarioVendedor(username);
        return productService.create(request);
    }

    @GetMapping
    public List<ProductResponseDTO> list(@RequestParam(required = false) String q,
                                         @RequestParam(required = false) Long categoriaId) {
        if (q != null && !q.isBlank()) {
            return productService.search(q);
        }
        if (categoriaId != null) {
            return productService.findByCategory(categoriaId);
        }
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ProductResponseDTO byId(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PutMapping("/{id}")
    public ProductResponseDTO update(@PathVariable Long id,
                                     @Valid @RequestBody ProductRequestDTO request,
                                     @RequestHeader("Authorization") String token) {
        String username = validarRolVendedor(token);
        request.setUsuarioVendedor(username);
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestHeader("Authorization") String token) {
        validarRolVendedor(token);
        productService.delete(id);
    }

    private String validarRolVendedor(String token) {
        Map<String, Object> respuesta = userClient.validate(token);
        Object valid = respuesta.get("valid");
        if (!Boolean.TRUE.equals(valid)) {
            throw new IllegalArgumentException("Token invalido para gestionar productos");
        }

        Object roles = respuesta.get("roles");
        if (roles == null || !roles.toString().contains("SELLER")) {
            throw new IllegalArgumentException("Solo un usuario con rol SELLER puede publicar productos");
        }

        return String.valueOf(respuesta.get("username"));
    }
}
