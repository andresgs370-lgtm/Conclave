package co.edu.unbosque.conclave.security;

import co.edu.unbosque.conclave.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Clase utilitaria para operaciones con JSON Web Tokens (JWT).
 * Proporciona métodos para generar, validar y extraer información de tokens JWT.
 *
 * <p>Usa la API de <strong>JJWT 0.12.x</strong>: {@code Jwts.builder()},
 * {
 * @code Jwts.parser().verifyWith(key).build()}.</p>
 */
@Component
public class JwtUtil {

	/** Tiempo de validez del token JWT en milisegundos (24 horas). */
	private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;

	/**
	 * Clave secreta para firmar los tokens JWT.
	 * Se lee de {@code jwt.secret} en {@code application.properties};
	 * si no se define, se usa el valor por defecto (solo para desarrollo).
	 */
	@Value("${jwt.secret:defaultSecretKeyWhichShouldBeAtLeast32CharactersLong}")
	private String secret;

	/**
	 * Construye la clave de firma HMAC-SHA a partir del secreto configurado.
	 *
	 * @return clave de firma
	 */
	public SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Extrae el nombre de usuario (subject) del token JWT.
	 *
	 * @param token token JWT
	 * @return nombre de usuario contenido en el token
	 */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Extrae la fecha de expiración del token JWT.
	 *
	 * @param token token JWT
	 * @return fecha de expiración
	 */
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	/**
	 * Extrae el rol del usuario del token JWT.
	 *
	 * @param token token JWT
	 * @return rol del usuario (p.ej. {@code "ADMIN"})
	 */
	public String extractRole(String token) {
		return extractClaim(token, claims -> claims.get("role", String.class));
	}

	/**
	 * Método genérico para extraer cualquier claim del token JWT.
	 *
	 * @param token          token JWT
	 * @param claimsResolver función que resuelve el claim específico
	 * @param <T>            tipo del claim
	 * @return valor del claim
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Extrae todos los claims del token JWT (JJWT 0.12 API).
	 *
	 * @param token token JWT
	 * @return todos los claims del token
	 */
	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	/**
	 * Verifica si el token ha expirado.
	 *
	 * @param token token JWT
	 * @return {@code true} si el token ha expirado
	 */
	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	/**
	 * Valida que el token pertenezca al usuario indicado y no haya expirado.
	 *
	 * @param token       token JWT a validar
	 * @param userDetails detalles del usuario contra los que validar
	 * @return {@code true} si el token es válido para ese usuario
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	/**
	 * Genera un token JWT firmado con HS256 para el usuario dado.
	 * Incluye los claims {@code authorities} y {@code role}.
	 *
	 * @param userDetails detalles del usuario autenticado
	 * @return token JWT compacto
	 */
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("authorities", userDetails.getAuthorities());

		// Añadir el nombre del rol si userDetails es nuestra clase User
		if (userDetails instanceof User user) {
			claims.put("role", user.getRole().name());
		}

		return createToken(claims, userDetails.getUsername());
	}

	/**
	 * Crea el token JWT con los claims y subject indicados (JJWT 0.12 API).
	 *
	 * @param claims  mapa de claims adicionales
	 * @param subject asunto del token (nombre de usuario)
	 * @return token JWT compacto
	 */
	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder()
				.claims(claims)
				.subject(subject)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
				.signWith(getSigningKey())
				.compact();
	}

}
