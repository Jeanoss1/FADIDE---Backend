package pe.com.fadide.sisco.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * Página de resultados que devuelven los endpoints GET /api/{recurso}/page.
 * Se usa un DTO propio en lugar de serializar Page de Spring para que el JSON sea estable
 * y el frontend solo dependa de estos cinco campos.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    // Factory: convierte una página de entidades en una página de DTO
    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
