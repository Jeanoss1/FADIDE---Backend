# Control de excepciones en SISCO

## 1. Idea general

Ningún controlador tiene bloques `try/catch`. Los servicios **lanzan** excepciones con significado
de negocio y una sola clase, `GlobalExceptionHandler` (`@RestControllerAdvice`), las **traduce** a una
respuesta HTTP con un cuerpo JSON uniforme. Es una aplicación de AOP: el manejo de errores es un
aspecto transversal separado de la lógica de negocio.

```
Controller ──► Service ──► throw new ResourceNotFoundException("Cliente no encontrado con id 9")
                                     │
                                     ▼
                  GlobalExceptionHandler.handleNotFound()
                                     │
                                     ▼
 404 { "timestamp": "2026-11-12T10:15:30", "status": 404, "error": "Not Found",
       "message": "Cliente no encontrado con id 9", "path": "/api/clientes/9" }
```

El frontend Angular (`core/utils/http-error.ts`) lee siempre `message` y, si existe, `errores`.

## 2. Excepciones propias (paquete `config`)

| Excepción | Cuándo se lanza | HTTP |
|-----------|-----------------|------|
| `ResourceNotFoundException` | El id no existe (cliente, obra, contrato...) | 404 |
| `ConflictException` | Regla de negocio: RUC repetido, borrar una obra con contrato | 409 |
| `BadRequestException` | Dato coherente en formato pero inválido para el negocio (RUC que no es cliente) | 400 |

## 3. Tabla completa de handlers

| Excepción capturada | Origen | HTTP | Mensaje |
|---------------------|--------|------|---------|
| `ResourceNotFoundException` | Servicios | 404 | El de la excepción |
| `NoResourceFoundException` | Ruta inexistente (`/api/xyz`) | 404 | "La ruta /api/xyz no existe" |
| `ConflictException` | Servicios | 409 | El de la excepción |
| `DataIntegrityViolationException` | PostgreSQL (unique / FK) | 409 | Restricción de datos |
| `MethodArgumentNotValidException` | `@Valid` en el DTO | 400 | "Datos inválidos" + `errores` por campo |
| `ConstraintViolationException` | Validación del `@Entity` al guardar | 400 | "Datos inválidos" + `errores` |
| `TransactionSystemException` | Validación del `@Entity` al hacer commit de un UPDATE | 400 | Se desenvuelve la causa |
| `BadRequestException` | Servicios | 400 | El de la excepción |
| `HttpMessageNotReadableException` | JSON mal formado o tipo incorrecto | 400 | JSON inválido |
| `MethodArgumentTypeMismatchException` | `/api/clientes/abc` | 400 | Parámetro inválido |
| `AuthenticationException` | Credenciales | 401 | Credenciales inválidas |
| `AccessDeniedException` | `@PreAuthorize` rechaza el rol | 403 | Sin permisos |
| `HttpRequestMethodNotSupportedException` | `PATCH` donde no existe | 405 | Método no permitido |
| `ResponseStatusException` | `AuthServiceImpl` (401, 403, 428) | El indicado | El indicado |
| `Exception` | Cualquier otra (bug, BD caída) | 500 | Mensaje genérico; el detalle va al log |

Spring elige el handler de la excepción **más específica**; `Exception.class` solo actúa como red
de seguridad. Por eso `AccessDeniedException` tiene su propio handler: si no, caería en el 500.

## 4. Errores que ocurren antes del controlador

Los filtros de Spring Security se ejecutan **antes** de llegar al `DispatcherServlet`, por lo que
`@RestControllerAdvice` no los ve. Para mantener el mismo formato JSON:

| Situación | Clase | HTTP |
|-----------|-------|------|
| Sin token, token expirado o firma inválida | `JwtAuthenticationEntryPoint` | 401 |
| Token válido pero el rol no puede entrar a la ruta | `JwtAccessDeniedHandler` | 403 |

El `JwtAuthenticationFilter` guarda el motivo exacto (`jwt_error`) en la petición para que el
401 diga por qué falló ("El token expiró, inicia sesión nuevamente").

## 5. Ejemplos de respuesta

**400 (validación del DTO)**

```json
{
  "timestamp": "2026-11-12T10:20:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Datos inválidos",
  "path": "/api/clientes",
  "errores": {
    "ruc": "El RUC debe tener 11 dígitos y empezar con 10, 15, 17 o 20",
    "razonSocial": "La razón social es obligatoria"
  }
}
```

**409 (regla de negocio)**

```json
{ "status": 409, "error": "Conflict", "message": "Ya existe un cliente con el RUC 20123456789", "path": "/api/clientes" }
```

**401 (token expirado)**

```json
{ "status": 401, "error": "Unauthorized", "message": "El token expiró, inicia sesión nuevamente", "path": "/api/proyectos" }
```

## 6. Relación con la implementación del curso

| Curso (`com.upn`) | SISCO |
|-------------------|-------|
| `ModelNotFoundException` | `ResourceNotFoundException` |
| `CustomErrorTemplate(datetime, message, details)` | Mapa `timestamp, status, error, message, path, errores` |
| `ResponseExceptionHandler extends ResponseEntityExceptionHandler` | `GlobalExceptionHandler` con `@ExceptionHandler` explícitos |
| `JwtAuthenticationEntryPoint` | `JwtAuthenticationEntryPoint` + `JwtAccessDeniedHandler` |
