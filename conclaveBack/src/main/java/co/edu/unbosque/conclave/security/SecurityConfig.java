package co.edu.unbosque.conclave.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración central de Spring Security.
 *
 * <ul>
 *   <li>Sin estado (stateless): ninguna sesión HTTP, autenticación exclusivamente por JWT.</li>
 *   <li>CSRF deshabilitado (API REST sin estado).</li>
 *   <li>CORS habilitado para los orígenes declarados en {@link #corsConfigurationSource()}.</li>
 *   <li>Peticiones sin token reciben HTTP 401 (no redirigen al login).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	/** Filtro de autenticación JWT que procesa los tokens en las solicitudes. */
	private final JwtAuthenticationFilter jwtAuthFilter;

	/** Servicio que carga los detalles del usuario para la autenticación. */
	private final UserDetailsService userDetailsService;

	/**
	 * Constructor que inicializa los componentes necesarios para la seguridad.
	 *
	 * @param jwtAuthFilter      filtro para procesar tokens JWT
	 * @param userDetailsService servicio para cargar detalles de usuarios
	 */
	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
			UserDetailsService userDetailsService) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Configura la cadena de filtros de seguridad HTTP.
	 *
	 * @param http configuración de seguridad HTTP
	 * @return cadena de filtros configurada
	 * @throws Exception si ocurre un error durante la configuración
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.exceptionHandling(ex ->
					ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
			.authorizeHttpRequests(auth -> auth
					// Autenticación pública
					.requestMatchers("/auth/**").permitAll()
					// Documentación OpenAPI / Swagger
					.requestMatchers(
							"/", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
					).permitAll()
					// Perfil propio: USER o ADMIN
					.requestMatchers("/user/me").hasAnyRole("USER", "ADMIN")
					// Gestión de usuarios: solo ADMIN
					.requestMatchers("/user/**").hasRole("ADMIN")
					// Gestión de API keys: solo ADMIN
					.requestMatchers("/admin/**").hasRole("ADMIN")
					// Prompts e historial propio: USER o ADMIN
					.requestMatchers("/prompt/**", "/history/me", "/history/me/**")
							.hasAnyRole("USER", "ADMIN")
					// Historial global: solo ADMIN
					.requestMatchers("/history/**").hasRole("ADMIN")
					// Cualquier otra ruta requiere autenticación
					.anyRequest().authenticated()
			)
			.sessionManagement(session ->
					session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authenticationProvider(authenticationProvider())
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Configura CORS para los orígenes del frontend y la propia API.
	 *
	 * @return fuente de configuración CORS
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(
				"http://localhost:8080",
				"http://localhost:8081",
				"http://localhost:5173"
		));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		// Sin allowCredentials para no requerir cookies de sesión

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	/**
	 * Configura el proveedor de autenticación DAO con el servicio de usuarios
	 * y el codificador de contraseñas.
	 *
	 * @return proveedor de autenticación configurado
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider =
				new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	/**
	 * Expone el {@link AuthenticationManager} como bean para usarlo en {@code AuthController}.
	 *
	 * @param config configuración de autenticación de Spring
	 * @return gestor de autenticación
	 * @throws Exception si ocurre un error al obtenerlo
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
			throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * Codificador de contraseñas BCrypt.
	 *
	 * @return instancia de {@link BCryptPasswordEncoder}
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
