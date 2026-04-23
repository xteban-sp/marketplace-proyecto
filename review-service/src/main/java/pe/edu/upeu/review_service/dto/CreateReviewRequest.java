package pe.edu.upeu.review_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateReviewRequest {

    @NotNull
    @JsonAlias("orderId")
    private UUID pedidoId;

    @NotNull
    @JsonAlias("productId")
    private Long productoId;

    @NotNull
    @JsonAlias("username")
    private UUID usuarioId;

    @NotNull
    @Min(1)
    @Max(5)
    @JsonAlias("rating")
    private Integer puntuacion;

    @NotBlank
    @JsonAlias("comment")
    private String comentario;

    public UUID getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(UUID pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
