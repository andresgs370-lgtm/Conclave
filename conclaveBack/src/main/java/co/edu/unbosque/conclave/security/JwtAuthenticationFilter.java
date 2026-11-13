package co.edu.unbosque.conclave.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT que intercepta las solicitudes HTTP exactamente una vez.
 *
 * <p>Extrae el token del header {@code Authorization: Bearer <token>}, lo valida y,
 * si es válido <em>y</em> el usuario está habilitado, establece la autenticación en
 * el {@link SecurityContextHolder}.</p>
 *
 * <p>Un usuario deshabilitado por el administrador no podrá seguir usando su token
 * aunque este aún no haya expirado.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	/** Utilidad para operaciones con tokens JWT. */
	private final JwtUtil jwtUtil;

	/** Servicio para cargar los detalles del usuario. */
	private final UserDetailsService userDetailsService;

	/**
	 * Constructor que inicializa las dependencias del filtro.
	 *
	 * @param jwtUtil            utilidad para tokens JWT
	 * @param userDetailsService servicio para cargar detalles de usuarios
	 */
	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Lógica principal del filtro.
	 *
	 * <ol>
	 *   <li>Extrae el token del header {@code Authorization}.</li>
	 *   <li>Extrae el username del token.</li>
	 *   <li>Carga el usuario y valida el token.</li>
	 *   <li>Solo autentica si {@code userDetails.isEnabled()} es {@code true}.</li>
	 *   <li>Continúa con la cadena de filtros.</li>
	 * </ol>
	 *
	 * @param request     solicitud HTTP entrante
	 * @param response    respuesta HTTP saliente
	 * @param filterChain cadena de filtros
	 * @throws ServletException si ocurre un error durante el procesamiento
	 * @throws IOException      si ocurre un error de E/S
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		final String authorizationHeader = request.getHeader("Authorization");

		String username = null;
		String jwt = null;

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			jwt = authorizationHeader.substring(7);
			try {
				username = jwtUtil.extractUsername(jwt);
			} catch (Exception e) {
				logger.error("Error al extraer el username del token JWT", e);
			}
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

			// Solo autentica si el token es válido Y el usuario está habilitado
			if (jwtUtil.validateToken(jwt, userDetails) && userDetails.isEnabled()) {
				UsernamePasswordAuthenticationToken authenticationToken =
						new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
				authenticationToken.setDetails(
						new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
		}

		filterChain.doFilter(request, response);
	}

}
