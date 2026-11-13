package co.edu.unbosque.conclave.security;

import co.edu.unbosque.conclave.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de {@link UserDetailsService} que carga usuarios desde
 * {@link UserRepository}.
 *
 * <p>La entidad {@code User} implementa directamente {@code UserDetails},
 * por lo que no se necesita ningún adaptador adicional.</p>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	/** Repositorio de usuarios utilizado para buscar información de usuarios. */
	private final UserRepository userRepository;

	/**
	 * Constructor que inicializa el repositorio de usuarios.
	 *
	 * @param userRepository repositorio de usuarios
	 */
	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Carga los detalles de un usuario por su nombre de usuario.
	 *
	 * @param username nombre de usuario a buscar
	 * @return detalles del usuario encontrado
	 * @throws UsernameNotFoundException si no existe un usuario con ese nombre
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
				.orElseThrow(() ->
						new UsernameNotFoundException("Usuario no encontrado: " + username));
	}


}
