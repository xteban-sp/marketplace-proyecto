package pe.edu.upeu.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CategoryRequestDTO {
    @NotBlank(message = "El nombre de categoria es obligatorio")
    private String nombre;
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
