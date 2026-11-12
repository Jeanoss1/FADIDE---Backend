# Seguridad con JWT en SISCO

## 1. ¿Qué es un JWT?

Un **JSON Web Token** es una cadena firmada con tres partes separadas por puntos:

```
eyJhbGciOiJIUzI1NiJ9 . eyJzdWIiOiJhZG1pbkBmYWRpZGUuY29tIiwicm9sIjoiQURNSU4iLCJleHAiOjE3...} . Xk3...firma
       HEADER                              PAYLOAD (claims)                                   FIRMA
```

| Parte   | Contenido en SISCO |
|---------|--------------------|
| Header  | `{"alg":"HS256"}`: algoritmo HMAC-SHA256 |
| Payload | `sub` (correo), `rol`, `iat` (emitido), `exp` (expira, 24 h por defecto) |
| Firma   | HMAC del header + payload con `sisco.jwt.secret`; si alguien cambia el payload, la firma deja de coincidir |

El payload **no está cifrado** (solo codificado en Base64URL): nunca se guardan contraseñas en él.
La seguridad está en la firma, que solo el servidor puede generar.

## 2. Clases que intervienen

| Clase | Responsabilidad |
|-------|-----------------|
| `AuthController` | `POST /api/auth/login`, `/register`, `/google`, `GET /api/auth/me`, `POST /api/auth/logout` |
| `AuthServiceImpl` | Autentica con `AuthenticationManager` (BCrypt) y genera el token |
| `JwtService` | Genera el token y lo valida (formato, firma y expiración) |
| `JwtCookieService` | Crea, lee y elimina la cookie HttpOnly `SISCO_TOKEN` |
| `JwtAuthenticationFilter` | En cada petición obtiene el token, lo valida y autentica al usuario |
| `CustomUserDetailsService` / `UsuarioDetails` | Carga el usuario por correo y expone su rol como `ROLE_<ROL>` |
| `SecurityConfig` | Rutas públicas/protegidas por rol, CORS, sesión STATELESS, manejo de 401/403 |
| `JwtAuthenticationEntryPoint` | Responde **401** en JSON (sin token, token expirado o firma inválida) |
| `JwtAccessDeniedHandler` | Responde **403** en JSON (token válido pero rol sin permiso) |

## 3. Flujo completo

```
 Angular                         Spring Boot
 ───────                         ───────────
 1. POST /api/auth/login  ─────► AuthController → AuthenticationManager
    {correo, password}             (CustomUserDetailsService + BCrypt)
                                   JwtService.generateToken(correo, rol)
 2. ◄──── 200 {token, rol, ...}  + Set-Cookie: SISCO_TOKEN=...; HttpOnly; SameSite=Lax

 3. GET /api/clientes     ─────► JwtAuthenticationFilter
    Authorization: Bearer <jwt>    ├─ resolverToken(): cabecera Bearer o cookie
                                   ├─ JwtService.validarToken(): firma + exp
                                   ├─ CustomUserDetailsService.loadUserByUsername()
                                   └─ SecurityContextHolder ← usuario + ROLE_GESTOR
                                 SecurityConfig: ¿el rol puede entrar a la ruta?
                                 @PreAuthorize en el controlador: ¿puede ejecutar la operación?
 4. ◄──── 200 [ ...clientes ]

    Token ausente / expirado  ──► 401 {"message":"El token expiró, inicia sesión nuevamente"}
    Rol sin permiso           ──► 403 {"message":"Tu rol no tiene permisos ..."}
```

`GET /api/auth/me` es el **comprobante de inicio de sesión**: el frontend lo llama al iniciar
la aplicación; si el token sigue siendo válido devuelve el usuario y, si no, responde 401 y el
frontend cierra la sesión.

## 4. Authorization Bearer vs. cookie HttpOnly

SISCO soporta los dos enfoques. El login devuelve el token en el cuerpo **y** en la cookie;
el filtro acepta cualquiera de los dos (primero la cabecera).

| Criterio | `Authorization: Bearer` (localStorage) | Cookie `HttpOnly` |
|----------|----------------------------------------|-------------------|
| Dónde vive el token | `localStorage`, lo lee JavaScript | Cookie del navegador, JavaScript **no** la puede leer |
| Cómo viaja | El interceptor de Angular agrega la cabecera | El navegador la envía sola (`withCredentials: true`) |
| Riesgo XSS (script malicioso) | Alto: el script puede leer y robar el token | Bajo: `document.cookie` no la expone |
| Riesgo CSRF (otro sitio dispara peticiones) | Nulo: otro sitio no conoce el token | Existe; se mitiga con `SameSite=Lax`/`Strict` y CORS restringido |
| Clientes no navegador (Postman, móvil) | Muy simple | Requiere manejar cookies |
| CORS | Basta permitir la cabecera `Authorization` | `allowCredentials=true` y orígenes explícitos (no `*`) |
| Logout | El frontend borra el token | El servidor envía la cookie con `Max-Age=0` (`POST /api/auth/logout`) |
| Producción | Funciona con HTTP o HTTPS | Debe usar `Secure` (solo HTTPS): `sisco.jwt.cookie-secure=true` |

**Decisión del equipo:** el frontend usa Bearer (lo exige el interceptor visto en clase y facilita
las pruebas con Postman/Swagger) y además envía `withCredentials`, de modo que la cookie HttpOnly
queda como respaldo y se puede demostrar el enfoque más seguro frente a XSS.

## 5. Pruebas rápidas

```http
### Login (ver el header Set-Cookie en la respuesta)
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{ "correo": "admin@fadide.com", "password": "Admin123." }

### Comprobante de sesión
GET http://localhost:8080/api/auth/me
Authorization: Bearer {{token}}

### Sin token -> 401 en JSON
GET http://localhost:8080/api/clientes

### Token alterado -> 401 "La firma del token no es válida"
GET http://localhost:8080/api/clientes
Authorization: Bearer {{token}}x

### SUPERVISOR intentando crear -> 403
POST http://localhost:8080/api/clientes
Authorization: Bearer {{tokenSupervisor}}
Content-Type: application/json

{ "razonSocial": "Prueba", "ruc": "20123456789" }

### Logout (elimina la cookie)
POST http://localhost:8080/api/auth/logout
```
