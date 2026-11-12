# Validaciones en SISCO (DTO + Modelo)

SISCO valida los datos en **tres niveles**. Cada nivel protege algo distinto:

```
 Angular (Validators)  ──►  DTO (@Valid)  ──►  Servicio (reglas)  ──►  Modelo @Entity  ──►  PostgreSQL
  experiencia de usuario     contrato API       negocio               integridad JPA        restricciones
```

| Nivel | Dónde | Qué valida | Error |
|-------|-------|-----------|-------|
| DTO | `dto/*Request` + `@Valid` en el controlador | Formato de la petición: obligatorio, longitudes, patrones (RUC, teléfono), rangos, fechas | 400 `MethodArgumentNotValidException` |
| Servicio | `service/*` | Reglas que requieren consultar la BD: RUC único, licencia única por obra, pagos ≤ monto del contrato | 409 `ConflictException` / 400 `BadRequestException` |
| Modelo | `model/*` (`@Entity`) | Que nunca se persista un objeto inválido, aunque se cree desde otro servicio o desde `DataSeeder` | 400 `ConstraintViolationException` |
| Base de datos | `@Column(nullable, unique, length)` | Última barrera (unique, not null, FK) | 409 `DataIntegrityViolationException` |

## ¿Por qué validar en el DTO **y** en el modelo?

- El **DTO** describe lo que el cliente envía: puede tener reglas más estrictas para la entrada
  (por ejemplo, el RUC debe empezar con 10, 15, 17 o 20, o la fecha de vencimiento debe ser
  posterior a la de emisión con `@AssertTrue`).
- El **modelo** describe lo que es válido guardar: Hibernate Validator ejecuta sus anotaciones
  automáticamente en `@PrePersist` y `@PreUpdate`. Protege la integridad aunque el dato no venga
  de un controlador.
- `@Column(nullable = false, length = 150)` solo genera el DDL; `@NotBlank` y `@Size(max = 150)`
  validan en Java **antes** del `INSERT` y devuelven un mensaje legible.

## Entidades validadas

| Entidad (equivalente en el curso) | DTO | Modelo |
|-----------------------------------|-----|--------|
| `Cliente` (Customer) | razón social 3-150, RUC 11 dígitos (10/15/17/20), teléfono 9 dígitos que empieza con 9 | `@NotBlank`, `@Size`, `@Pattern` |
| `Contratista` (Category) | nombre 3-150, RUC | `@NotBlank`, `@Size`, `@Pattern` |
| `Proyecto` (Room: tiene FK a Cliente) | nombre, ubicación, `idCliente` positivo, ids de contratistas positivos | `@NotBlank`, `@NotNull` en estado y cliente |
| `Licencia` | número, fechas, `@AssertTrue` vencimiento > emisión | `@NotBlank`, `@NotNull` |
| `Contrato`, `Pago`, `Penalidad` | Montos positivos, `@Digits`, fechas obligatorias | Mismas reglas en el modelo |
| `Supervision`, `Observacion`, `Evidencia` | Avance 0-100, fechas no futuras, mensajes en español | — |

## Anotaciones usadas

| Anotación | Uso |
|-----------|-----|
| `@NotNull` | No puede ser `null` (números, fechas, relaciones) |
| `@NotBlank` | Texto no nulo y con al menos un carácter distinto de espacio |
| `@Size(min, max)` | Longitud del texto |
| `@Pattern(regexp)` | Formato (RUC, teléfono) |
| `@Positive`, `@DecimalMin`, `@DecimalMax`, `@Digits` | Montos y porcentajes |
| `@PastOrPresent` | Fechas que no pueden ser futuras |
| `@AssertTrue` | Regla entre dos campos del mismo DTO |
| `@Valid` | Activa la validación del `@RequestBody` en el controlador |
