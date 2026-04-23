package pe.edu.upeu.product_service.dto.response;

import java.math.BigDecimal;

public class ProductResponseDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String usuarioVendedor;
    private Long categoriaId;
    private String categoriaNombre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }
}
