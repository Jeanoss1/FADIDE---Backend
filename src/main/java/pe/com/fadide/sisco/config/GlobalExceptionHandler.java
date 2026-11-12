package pe.com.fadide.sisco.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Control de excepciones centralizado (patrón Advice / AOP).
 * Cualquier excepción lanzada en un controlador o servicio llega aquí y se traduce
 * a una respuesta JSON uniforme con el código HTTP correcto:
 * <pre>
 * { "timestamp": "...", "status": 404, "error": "Not Found",
 *   "message": "Cliente no encontrado con id 9", "path": "/api/clientes/9" }
 * </pre>
 * Los errores de validación agregan "errores": { campo: mensaje }.
 * Orden de resolución: Spring elige el handler cuya excepción sea la más específica;
 * Exception.class solo atrapa lo que ningún otro handler cubre (500).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- 404: recurso inexistente ----------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "La ruta /" + ex.getResourcePath() + " no existe", req);
    }

    // ---------- 409: conflicto con el estado de los datos ----------
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        // Restricción de la base de datos: valor único repetido o registro referenciado por otros registros
        return build(HttpStatus.CONFLICT,
                "La operación viola una restricción de datos: el valor ya existe o el registro está siendo usado por otros registros", req);
    }

    // ---------- 400: datos de entrada inválidos ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        // Validaciones del DTO (@Valid + @NotBlank, @Size, @Pattern...): un mensaje por campo
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return buildConErrores(errores, req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        // Validaciones del modelo (@Entity) que Hibernate Validator ejecuta antes de INSERT/UPDATE
        return buildConErrores(erroresDe(ex), req);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransaction(TransactionSystemException ex, HttpServletRequest req) {
        // Al actualizar, la validación del modelo ocurre en el commit y llega envuelta en esta excepción
        Throwable causa = ex.getRootCause();
        if (causa instanceof ConstraintViolationException cve) {
            return buildConErrores(erroresDe(cve), req);
        }
        return handleDefault(ex, req);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        // JSON mal formado o con tipos incorrectos (por ejemplo, texto en un campo numérico)
        return build(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido o tiene tipos incorrectos", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        // Parámetro de ruta o de consulta con tipo incorrecto, por ejemplo /api/clientes/abc
        return build(HttpStatus.BAD_REQUEST, "Valor inválido para el parámetro '" + ex.getName() + "': " + ex.getValue(), req);
    }

    // ---------- 401 / 403: seguridad ----------
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciales inválidas o sesión expirada", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        // @PreAuthorize rechazó al usuario: su rol no puede ejecutar esta operación
        return build(HttpStatus.FORBIDDEN, "Tu rol no tiene permisos para realizar esta operación", req);
    }

    // ---------- 405 ----------
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "El método " + ex.getMethod() + " no está permitido en esta ruta", req);
    }

    // ---------- Código definido por quien lanza la excepción (401, 403, 428...) ----------
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        return build(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), req);
    }

    // ---------- 500: cualquier error no previsto ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleDefault(Exception ex, HttpServletRequest req) {
        // Se registra el detalle en el log del servidor, pero no se expone al cliente
        log.error("Error no controlado en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno en el servidor", req);
    }

    private Map<String, String> erroresDe(ConstraintViolationException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getConstraintViolations()
                .forEach(v -> errores.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return errores;
    }

    private ResponseEntity<Map<String, Object>> buildConErrores(Map<String, String> errores, HttpServletRequest req) {
        ResponseEntity<Map<String, Object>> response = build(HttpStatus.BAD_REQUEST, "Datos inválidos", req);
        response.getBody().put("errores", errores);
        return response;
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, HttpServletRequest req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
