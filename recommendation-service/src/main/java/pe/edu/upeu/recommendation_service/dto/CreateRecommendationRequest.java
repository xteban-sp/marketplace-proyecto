package pe.edu.upeu.recommendation_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pe.edu.upeu.recommendation_service.entity.RecommendationSource;

import java.util.UUID;

public class CreateRecommendationRequest {

    @NotNull
    private UUID usuarioId;

    @NotNull
    private Long productoId;

    @NotNull
    @Min(0)
    @Max(100)
    private Double puntaje;

    @NotNull
    private RecommendationSource fuente;

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Double getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Double puntaje) {
        this.puntaje = puntaje;
    }

    public RecommendationSource getFuente() {
        return fuente;
    }

    public void setFuente(RecommendationSource fuente) {
        this.fuente = fuente;
    }
}
