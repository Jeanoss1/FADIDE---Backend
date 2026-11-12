package pe.com.fadide.sisco.dto;

import java.util.List;

// Vista consolidada de una obra para el portal del cliente
public record MiObraResponse(
        ProyectoResponse proyecto,
        LicenciaResponse licencia,
        List<ContratoAvance> contratos
) {
    public record ContratoAvance(
            ContratoResponseDTO contrato,
            SupervisionResponse supervision,
            List<EvidenciaResponse> evidencias,
            List<ObservacionResponse> observaciones
    ) {
    }
}
