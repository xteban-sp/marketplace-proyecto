package pe.edu.upeu.report_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.report_service.entity.OrderRecord;
import pe.edu.upeu.report_service.entity.ReviewRecord;
import pe.edu.upeu.report_service.entity.SalesRecord;
import pe.edu.upeu.report_service.repository.OrderRecordRepository;
import pe.edu.upeu.report_service.repository.ReviewRecordRepository;
import pe.edu.upeu.report_service.repository.SalesRecordRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Escucha los eventos de la saga (Kafka) y va acumulando datos para los reportes.
 * Es idempotente: si un evento ya se registró (mismo id de origen), se ignora.
 */
@Slf4j
@Component
public class ReportEventListener {

    private final SalesRecordRepository salesRepo;
    private final OrderRecordRepository orderRepo;
    private final ReviewRecordRepository reviewRepo;
    private final AdminNotifier adminNotifier;

    public ReportEventListener(SalesRecordRepository salesRepo,
                               OrderRecordRepository orderRepo,
                               ReviewRecordRepository reviewRepo,
                               AdminNotifier adminNotifier) {
        this.salesRepo = salesRepo;
        this.orderRepo = orderRepo;
        this.reviewRepo = reviewRepo;
        this.adminNotifier = adminNotifier;
    }

    @KafkaListener(topics = "pedido-creado", groupId = "report-service")
    public void onPedidoCreado(Map<String, Object> p) {
        String pedidoId = str(p.get("pedidoId"));
        if (pedidoId == null || orderRepo.existsByPedidoId(pedidoId)) return;
        orderRepo.save(OrderRecord.builder()
                .pedidoId(pedidoId)
                .compradorId(str(p.get("compradorId")))
                .vendedorId(str(p.get("vendedorId")))
                .total(dec(p.get("total")))
                .fecha(LocalDateTime.now())
                .build());
        log.info("[reporte] pedido registrado {}", pedidoId);
    }

    @KafkaListener(topics = "pago-aprobado", groupId = "report-service")
    public void onPagoAprobado(Map<String, Object> p) {
        if (guardarPago(p, "APPROVED")) {
            adminNotifier.notificarVenta(String.valueOf(dec(p.get("monto"))), str(p.get("pedidoId")));
        }
    }

    @KafkaListener(topics = "pago-fallido", groupId = "report-service")
    public void onPagoFallido(Map<String, Object> p) {
        guardarPago(p, "FAILED");
    }

    @KafkaListener(topics = "resena-creada", groupId = "report-service")
    public void onResenaCreada(Map<String, Object> p) {
        String resenaId = str(p.get("resenaId"));
        if (resenaId == null || reviewRepo.existsByResenaId(resenaId)) return;
        reviewRepo.save(ReviewRecord.builder()
                .resenaId(resenaId)
                .productoId(lng(p.get("productoId")))
                .usuarioId(str(p.get("usuarioId")))
                .puntuacion(intg(p.get("puntuacion")))
                .fecha(LocalDateTime.now())
                .build());
        log.info("[reporte] resena registrada {}", resenaId);
    }

    private boolean guardarPago(Map<String, Object> p, String estado) {
        String pagoId = str(p.get("pagoId"));
        if (pagoId == null || salesRepo.existsByPagoId(pagoId)) return false;
        salesRepo.save(SalesRecord.builder()
                .pagoId(pagoId)
                .pedidoId(str(p.get("pedidoId")))
                .compradorId(str(p.get("compradorId")))
                .vendedorId(str(p.get("vendedorId")))
                .monto(dec(p.get("monto")))
                .estado(estado)
                .fecha(LocalDateTime.now())
                .build());
        log.info("[reporte] pago {} registrado {}", estado, pagoId);
        return true;
    }

    // ---- helpers de conversión (los valores llegan como Number o String en el Map) ----
    private static String str(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o);
        return (s.isBlank() || "null".equalsIgnoreCase(s)) ? null : s;
    }
    private static BigDecimal dec(Object o) {
        try { return o == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(o)); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }
    private static Long lng(Object o) {
        try { return o == null ? null : Long.valueOf(String.valueOf(o).split("\\.")[0]); }
        catch (Exception e) { return null; }
    }
    private static Integer intg(Object o) {
        try { return o == null ? null : Integer.valueOf(String.valueOf(o).split("\\.")[0]); }
        catch (Exception e) { return null; }
    }
}
