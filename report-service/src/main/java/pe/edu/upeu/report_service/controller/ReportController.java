package pe.edu.upeu.report_service.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.report_service.service.ReportService;

import java.util.List;
import java.util.Map;

/**
 * Reportes del marketplace. TODO requiere rol ADMIN.
 * (report-service agrega los datos desde los eventos Kafka de la saga.)
 */
@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        return reportService.resumen();
    }

    @GetMapping("/ventas")
    public Map<String, Object> ventas(@RequestParam(name = "dias", defaultValue = "30") int dias) {
        return reportService.ventas(dias);
    }

    @GetMapping("/top-vendedores")
    public List<Map<String, Object>> topVendedores(@RequestParam(name = "limite", defaultValue = "5") int limite) {
        return reportService.topVendedores(limite);
    }

    @GetMapping("/top-productos")
    public List<Map<String, Object>> topProductos(@RequestParam(name = "limite", defaultValue = "5") int limite) {
        return reportService.topProductos(limite);
    }

    @GetMapping("/resenas")
    public Map<String, Object> resenas() {
        return reportService.resenas();
    }
}
