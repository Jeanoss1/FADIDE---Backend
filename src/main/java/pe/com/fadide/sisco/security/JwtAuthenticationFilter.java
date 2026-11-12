package pe.com.fadide.sisco.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Se ejecuta una vez por petición, antes de UsernamePasswordAuthenticationFilter.
 * 1. Obtiene el token: cabecera "Authorization: Bearer ..." o, si no existe, la cookie HttpOnly.
 * 2. Valida firma y expiración con JwtService.
 * 3. Si es válido, carga el usuario y lo registra en el SecurityContext (queda autenticado).
 * 4. Si no es válido, guarda el motivo en la petición para que el
 *    JwtAuthenticationEntryPoint responda 401 con ese mensaje.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final JwtCookieService jwtCookieService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = resolverToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String correo = jwtService.validarToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(correo);

                if (!userDetails.isEnabled()) {
                    throw new TokenInvalidoException("La cuenta del usuario está desactivada");
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (TokenInvalidoException ex) {
                request.setAttribute(JwtAuthenticationEntryPoint.ATTR_ERROR, ex.getMessage());
            } catch (UsernameNotFoundException ex) {
                request.setAttribute(JwtAuthenticationEntryPoint.ATTR_ERROR, "El usuario del token ya no existe");
            }
        }

        filterChain.doFilter(request, response);
    }

    // Prioridad: cabecera Authorization (Bearer) y luego cookie HttpOnly
    private String resolverToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER)) {
            return header.substring(BEARER.length());
        }
        return jwtCookieService.leer(request);
    }
}
