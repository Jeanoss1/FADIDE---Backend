package pe.com.fadide.sisco.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import pe.com.fadide.sisco.config.BadRequestException;
import pe.com.fadide.sisco.config.ResourceNotFoundException;
import pe.com.fadide.sisco.dto.ArchivoDescarga;
import pe.com.fadide.sisco.dto.ArchivoGuardado;
import pe.com.fadide.sisco.dto.EvidenciaPenalidadResponseDTO;
import pe.com.fadide.sisco.model.EvidenciaPenalidad;
import pe.com.fadide.sisco.model.Penalidad;
import pe.com.fadide.sisco.model.TipoArchivo;
import pe.com.fadide.sisco.repository.EvidenciaPenalidadRepository;
import pe.com.fadide.sisco.repository.PenalidadRepository;
import pe.com.fadide.sisco.service.AlmacenamientoService;
import pe.com.fadide.sisco.service.EvidenciaPenalidadService;

@Service
@RequiredArgsConstructor
public class EvidenciaPenalidadServiceImpl implements EvidenciaPenalidadService {

    private final EvidenciaPenalidadRepository evidenciaPenalidadRepository;
    private final PenalidadRepository penalidadRepository;
    private final AlmacenamientoService almacenamiento;

    @Override
    @Transactional(readOnly = true)
    public List<EvidenciaPenalidadResponseDTO> listar(Long idPenalidad) {
        buscarPenalidad(idPenalidad);
        return evidenciaPenalidadRepository
                .findByPenalidad_IdPenalidadOrderByFechaDescIdEvidenciaPenalidadDesc(idPenalidad).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public EvidenciaPenalidadResponseDTO subir(Long idPenalidad, MultipartFile archivo, String descripcion, LocalDate fecha) {
        if (fecha == null) {
            throw new BadRequestException("La fecha es obligatoria");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha no puede ser futura");
        }
        if (descripcion != null && descripcion.length() > 1000) {
            throw new BadRequestException("La descripción no puede superar 1000 caracteres");
        }
        Penalidad penalidad = buscarPenalidad(idPenalidad);
        ArchivoGuardado guardado = almacenamiento.guardar(archivo);
        try {
            EvidenciaPenalidad evidencia = EvidenciaPenalidad.builder()
                    .nombreOriginal(guardado.nombreOriginal())
                    .archivo(guardado.nombreGuardado())
                    .tipoContenido(guardado.tipo().contentType())
                    .tamanoBytes(guardado.tamano())
                    .descripcion(StringUtils.hasText(descripcion) ? descripcion.trim() : null)
                    .fecha(fecha)
                    .penalidad(penalidad)
                    .build();
            return toDTO(evidenciaPenalidadRepository.save(evidencia));
        } catch (RuntimeException e) {
            almacenamiento.eliminar(guardado.nombreGuardado()); // no dejar archivos huérfanos
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoDescarga descargar(Long idEvidenciaPenalidad) {
        EvidenciaPenalidad evidencia = buscar(idEvidenciaPenalidad);
        TipoArchivo tipo = TipoArchivo.porExtension(StringUtils.getFilenameExtension(evidencia.getArchivo()))
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de archivo desconocido"));
        return new ArchivoDescarga(almacenamiento.cargar(evidencia.getArchivo()), evidencia.getNombreOriginal(),
                tipo, evidencia.getTamanoBytes());
    }

    @Override
    @Transactional
    public void eliminar(Long idEvidenciaPenalidad) {
        EvidenciaPenalidad evidencia = buscar(idEvidenciaPenalidad);
        evidenciaPenalidadRepository.delete(evidencia);
        evidenciaPenalidadRepository.flush();
        almacenamiento.eliminar(evidencia.getArchivo());
    }

    private EvidenciaPenalidad buscar(Long id) {
        return evidenciaPenalidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidencia de penalidad no encontrada con id " + id));
    }

    private Penalidad buscarPenalidad(Long idPenalidad) {
        return penalidadRepository.findById(idPenalidad)
                .orElseThrow(() -> new ResourceNotFoundException("Penalidad no encontrada con id " + idPenalidad));
    }

    private EvidenciaPenalidadResponseDTO toDTO(EvidenciaPenalidad e) {
        String categoria = TipoArchivo.porContentType(e.getTipoContenido())
                .map(t -> t.categoria().name()).orElse("ARCHIVO");
        return EvidenciaPenalidadResponseDTO.builder()
                .idEvidenciaPenalidad(e.getIdEvidenciaPenalidad())
                .nombreOriginal(e.getNombreOriginal())
                .categoria(categoria)
                .tipoContenido(e.getTipoContenido())
                .tamanoBytes(e.getTamanoBytes())
                .descripcion(e.getDescripcion())
                .fecha(e.getFecha())
                .idPenalidad(e.getPenalidad().getIdPenalidad())
                .build();
    }
}
