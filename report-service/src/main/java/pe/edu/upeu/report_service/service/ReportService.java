package pe.edu.upeu.report_service.service;

import org.springframework.stereotype.Service;
import pe.edu.upeu.report_service.repository.OrderRecordRepository;
import pe.edu.upeu.report_service.repository.ReviewRecordRepository;
import pe.edu.upeu.report_service.repository.SalesRecordRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final SalesRecordRepository salesRepo;
    private final OrderRecordRepository orderRepo;
    private final ReviewRecordRepository reviewRepo;

    public ReportService(SalesRecordRepository salesRepo,
                         OrderRecordRepository orderRepo,
                         ReviewRecordRepository reviewRepo) {
        this.salesRepo = salesRepo;
        this.orderRepo = orderRepo;
        this.reviewRepo = reviewRepo;
    }

    /** Tarjetas del panel: ingresos, ventas, pedidos, reseñas, promedios. */
    public Map<String, Object> resumen() {
        BigDecimal ingresos = nvl(salesRepo.totalIngresos());
        long ventas = salesRepo.countByEstado("APPROVED");
        long fallidos = salesRepo.countByEstado("FAILED");
        long pedidos = orderRepo.count();
        long resenas = reviewRepo.count();
        Double promedio = reviewRepo.promedioGlobal();
        BigDecimal ticket = ventas > 0
                ? ingresos.divide(BigDecimal.valueOf(ventas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ingresosTotales", ingresos);
        m.put("ventasAprobadas", ventas);
        m.put("pagosFallidos", fallidos);
        m.put("ticketPromedio", ticket);
        m.put("totalPedidos", pedidos);
        m.put("totalResenas", resenas);
        m.put("promedioCalificacion", promedio == null ? 0.0 :
                BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP));
        return m;
    }

    /** Serie temporal de ingresos y ventas por día (últimos N días). */
    public Map<String, Object> ventas(int dias) {
        LocalDateTime desde = LocalDateTime.now().minusDays(Math.max(1, dias));
        List<Map<String, Object>> ingresosPorDia = new ArrayList<>();
        for (Object[] r : salesRepo.ventasPorDia(desde)) {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("dia", r[0]);
            d.put("ingresos", num(r[1]));
            d.put("ventas", num(r[2]));
            ingresosPorDia.add(d);
        }
        List<Map<String, Object>> pedidosPorDia = new ArrayList<>();
        for (Object[] r : orderRepo.pedidosPorDia(desde)) {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("dia", r[0]);
            d.put("pedidos", num(r[1]));
            d.put("monto", num(r[2]));
            pedidosPorDia.add(d);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("dias", dias);
        out.put("ingresosPorDia", ingresosPorDia);
        out.put("pedidosPorDia", pedidosPorDia);
        return out;
    }

    /** Ranking de vendedores por ingresos. */
    public List<Map<String, Object>> topVendedores(int limite) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : salesRepo.topVendedores(limite)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("vendedorId", r[0]);
            m.put("ingresos", num(r[1]));
            m.put("ventas", num(r[2]));
            out.add(m);
        }
        return out;
    }

    /** Productos más populares (por número de reseñas). */
    public List<Map<String, Object>> topProductos(int limite) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : reviewRepo.topProductos(limite)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productoId", num(r[0]));
            m.put("resenas", num(r[1]));
            m.put("promedio", num(r[2]));
            out.add(m);
        }
        return out;
    }

    /** Distribución de calificaciones. */
    public Map<String, Object> resenas() {
        List<Map<String, Object>> dist = new ArrayList<>();
        for (Object[] r : reviewRepo.distribucion()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("estrellas", num(r[0]));
            m.put("cantidad", num(r[1]));
            dist.add(m);
        }
        Double promedio = reviewRepo.promedioGlobal();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", reviewRepo.count());
        out.put("promedio", promedio == null ? 0.0 :
                BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP));
        out.put("distribucion", dist);
        return out;
    }

    private static BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private static Number num(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n;
        try { return new BigDecimal(String.valueOf(o)); } catch (Exception e) { return 0; }
    }
}
