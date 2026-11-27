package pe.com.fadide.sisco.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import pe.com.fadide.sisco.dto.ArchivoGuardado;
import pe.com.fadide.sisco.model.TipoArchivo;
import pe.com.fadide.sisco.config.BadRequestException;
import pe.com.fadide.sisco.config.ResourceNotFoundException;
import pe.com.fadide.sisco.dto.EvidenciaRequest;
import pe.com.fadide.sisco.dto.EvidenciaResponse;
import pe.com.fadide.sisco.model.Evidencia;
import pe.com.fadide.sisco.model.Supervision;
import pe.com.fadide.sisco.repository.EvidenciaRepository;
import pe.com.fadide.sisco.repository.SupervisionRepository;

// Patrón Singleton: bean de Spring con una única instancia
@Service
public class EvidenciaService {

    private final EvidenciaRepository evidenciaRepository;
    private final SupervisionRepository supervisionRepository;
    private final AlmacenamientoService almacenamiento;

    public EvidenciaService(EvidenciaRepository evidenciaRepository,
                            SupervisionRepository supervisionRepository,
                            AlmacenamientoService almacenamiento) {
        this.evidenciaRepository = evidenciaRepository;
        this.supervisionRepository = supervisionRepository;
        this.almacenamiento = almacenamiento;
    }

    /** Contenido de un archivo de evidencia listo para enviarse al navegador. */
    public record ArchivoEvidencia(Resource recurso, String nombre, TipoArchivo tipo, long tamano) {
    }

    @Transactional(readOnly = true)
    public List<EvidenciaResponse> listar(Long idSupervision) {
        List<Evidencia> evidencias = idSupervision == null
                ? evidenciaRepository.listarTodas()
                : evidenciaRepository.buscarPorSupervision(idSupervision);
        return evidencias.stream().map(EvidenciaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EvidenciaResponse obtener(Long id) {
        return EvidenciaResponse.from(buscar(id));
    }

    @Transactional
    public EvidenciaResponse crear(EvidenciaRequest request) {
        Evidencia evidencia = Evidencia.builder()
                .imagen(request.imagen().trim())
                .descripcion(request.descripcion())
                .fecha(request.fecha())
                .supervision(buscarSupervision(request.idSupervision()))
                .build();
        return EvidenciaResponse.from(evidenciaRepository.save(evidencia));
    }

    // Evidencia con archivo adjunto (imagen, PDF, Word, Excel, PowerPoint, plano DWG, texto)
    @Transactional
    public EvidenciaResponse crearConArchivo(MultipartFile archivo, String descripcion, LocalDate fecha, Long idSupervision) {
        if (fecha == null) {
            throw new BadRequestException("La fecha es obligatoria");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha no puede ser futura");
        }
        if (descripcion != null && descripcion.length() > 1000) {
            throw new BadRequestException("La descripción no puede superar 1000 caracteres");
        }
        Supervision supervision = buscarSupervision(idSupervision);
        ArchivoGuardado guardado = almacenamiento.guardar(archivo);
        try {
            Evidencia evidencia = Evidencia.builder()
                    .imagen(guardado.nombreOriginal())
                    .archivo(guardado.nombreGuardado())
                    .tipoContenido(guardado.tipo().contentType())
                    .tamanoBytes(guardado.tamano())
                    .descripcion(StringUtils.hasText(descripcion) ? descripcion.trim() : null)
                    .fecha(fecha)
                    .supervision(supervision)
                    .build();
            return EvidenciaResponse.from(evidenciaRepository.save(evidencia));
        } catch (RuntimeException e) {
            almacenamiento.eliminar(guardado.nombreGuardado()); // no dejar archivos huérfanos
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ArchivoEvidencia descargar(Long id) {
        Evidencia evidencia = buscar(id);
        if (evidencia.getArchivo() == null) {
            throw new ResourceNotFoundException("La evidencia " + id + " es un enlace y no tiene archivo adjunto");
        }
        TipoArchivo tipo = TipoArchivo.porExtension(StringUtils.getFilenameExtension(evidencia.getArchivo()))
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de archivo desconocido"));
        return new ArchivoEvidencia(almacenamiento.cargar(evidencia.getArchivo()), evidencia.getImagen(), tipo,
                evidencia.getTamanoBytes() == null ? -1 : evidencia.getTamanoBytes());
    }

    @Transactional
    public EvidenciaResponse actualizar(Long id, EvidenciaRequest request) {
        Evidencia evidencia = buscar(id);
        // En una evidencia con archivo, el nombre original no se reemplaza por una URL
        if (evidencia.getArchivo() == null) {
            evidencia.setImagen(request.imagen().trim());
        }
        evidencia.setDescripcion(request.descripcion());
        evidencia.setFecha(request.fecha());
        evidencia.setSupervision(buscarSupervision(request.idSupervision()));
        return EvidenciaResponse.from(evidenciaRepository.save(evidencia));
    }

    @Transactional
    public void eliminar(Long id) {
        Evidencia evidencia = buscar(id);
        evidenciaRepository.delete(evidencia);
        evidenciaRepository.flush();
        if (evidencia.getArchivo() != null) {
            almacenamiento.eliminar(evidencia.getArchivo());
        }
    }

    private Evidencia buscar(Long id) {
        return evidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada con id " + id));
    }

    private Supervision buscarSupervision(Long idSupervision) {
        return supervisionRepository.findById(idSupervision)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supervisión no encontrada con id " + idSupervision));
    }
}
