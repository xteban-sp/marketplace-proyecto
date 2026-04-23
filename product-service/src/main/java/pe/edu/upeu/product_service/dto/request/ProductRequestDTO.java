package pe.edu.upeu.product_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductRequestDTO {
    @NotBlank
    private String titulo;
    @NotBlank
    private String descripcion;
    @NotNull
    @DecimalMin("0.10")
    private BigDecimal precio;
    @NotNull
    @Min(0)
    private Integer stock;
    @NotBlank
    private String usuarioVendedor;
    @NotNull
    private Long categoriaId;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getUsuarioVendedor() { return usuarioVendedor; }
    public void setUsuarioVendedor(String usuarioVendedor) { this.usuarioVendedor = usuarioVendedor; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
}
