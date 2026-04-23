package pe.edu.upeu.recommendation_service.dto;

import pe.edu.upeu.recommendation_service.entity.RecommendationSource;

import java.time.LocalDateTime;
import java.util.UUID;

public class RecommendationResponse {
    private UUID id;
    private UUID usuarioId;
    private Long productoId;
    private Double puntaje;
    private RecommendationSource fuente;
    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
