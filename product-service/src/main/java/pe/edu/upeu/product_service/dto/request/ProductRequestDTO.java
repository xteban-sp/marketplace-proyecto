package pe.edu.upeu.product_service.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    @JsonAlias("titulo")
    private String name;

    @JsonAlias("descripcion")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "Formato de precio inválido")
    @JsonAlias("precio")
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Size(max = 300, message = "La URL de imagen no puede superar los 300 caracteres")
    @JsonAlias("imagenUrl")
    private String imageUrl;

    // El vendedor se toma del token JWT en el servidor; no se exige en el body.
    private UUID sellerId;

    @NotNull(message = "El ID de categoría es obligatorio")
    @JsonAlias("categoriaId")
    private Long categoryId;
}
