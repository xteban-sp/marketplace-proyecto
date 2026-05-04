package pe.edu.upeu.product_service.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @JsonAlias("nombre")
    private String name;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @JsonAlias("descripcion")
    private String description;
}
