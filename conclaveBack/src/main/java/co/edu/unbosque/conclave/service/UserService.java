package co.edu.unbosque.conclave.service;

import co.edu.unbosque.conclave.dto.UserDTO;
import co.edu.unbosque.conclave.exception.PasswordNotValidException;
import co.edu.unbosque.conclave.model.User;
import co.edu.unbosque.conclave.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Servicio que proporciona operaciones CRUD y funcionalidades adicionales para usuarios.
 * Implementa {@link CRUDOperation} para las operaciones básicas.
 */
@Service
public class UserService implements CRUDOperation<UserDTO> {

	/** Patrón de validación de contraseña:
	 * mínimo 8 caracteres, una minúscula, una mayúscula, un dígito y un símbolo. */
	private static final Pattern PASSWORD_PATTERN = Pattern.compile(
			"^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$");

	/** Repositorio para acceder a los datos de usuarios en la base de datos. */
	@Autowired
	private UserRepository userRepo;

	/** Mapeador para convertir entre entidades {@link User} y DTOs {@link UserDTO}. */
	@Autowired
	private ModelMapper modelMapper;

	/** Codificador para encriptar contraseñas de usuarios. */
	@Autowired
	private PasswordEncoder passwordEncoder;

	/** Constructor por defecto. */
	public UserService() {
	}

	/**
	 * Cuenta el número total de usuarios en la base de datos.
	 *
	 * @return número total de usuarios
	 */
	@Override
	public long count() {
		return userRepo.count();
	}

	/**
	 * Verifica si existe un usuario con el ID especificado.
	 *
	 * @param id ID del usuario a verificar
	 * @return {@code true} si el usuario existe
	 */
	@Override
	public boolean exist(Long id) {
		return userRepo.existsById(id);
	}

	/**
	 * Crea un nuevo usuario en la base de datos.
	 *
	 * <p>El rol del cliente siempre se fuerza a {@code USER} salvo que la entidad
	 * ya traiga uno distinto (uso interno del {@code LoadDatabase}).</p>
	 *
	 * <ul>
	 *   <li>Devuelve {@code 0}: usuario creado exitosamente.</li>
	 *   <li>Devuelve {@code 1}: el nombre de usuario ya existe.</li>
	 *   <li>Devuelve {@code 2}: el correo electrónico ya existe.</li>
	 *   <li>Devuelve {@code 3}: la contraseña no cumple los requisitos.</li>
	 * </ul>
	 *
	 * @param data DTO con los datos del usuario a crear
	 * @return código de resultado (0 = OK, 1 = username duplicado, 2 = email duplicado,
	 *         3 = contraseña inválida)
	 */
	@Override
	public int create(UserDTO data) {
		if (findUsernameAlreadyTaken(data.getUsername())) {
			return 1;
		}
		if (data.getEmail() != null && userRepo.existsByEmail(data.getEmail())) {
			return 2;
		}
		try {
			validatePassword(data.getPassword());
		} catch (PasswordNotValidException e) {
			return 3;
		}

		User entity = modelMapper.map(data, User.class);
		entity.setPassword(passwordEncoder.encode(data.getPassword()));

		// Si el DTO no especifica rol, ModelMapper puede dejar null; usamos USER por defecto
		if (entity.getRole() == null) {
			entity.setRole(User.Role.USER);
		}


		userRepo.save(entity);
		return 0;
	}

	/**
	 * Obtiene todos los usuarios de la base de datos.
	 *
	 * @return lista de DTOs de usuarios
	 */
	@Override
	public List<UserDTO> getAll() {
		List<User> entityList = userRepo.findAll();
		List<UserDTO> dtoList = new ArrayList<>();
		entityList.forEach(entity -> dtoList.add(modelMapper.map(entity, UserDTO.class)));
		return dtoList;
	}

	/**
	 * Elimina un usuario por su ID.
	 *
	 * <ul>
	 *   <li>Devuelve {@code 0}: eliminado correctamente.</li>
	 *   <li>Devuelve {@code 1}: usuario no encontrado.</li>
	 * </ul>
	 *
	 * @param id ID del usuario a eliminar
	 * @return código de resultado
	 */
	@Override
	public int deleteById(Long id) {
		Optional<User> found = userRepo.findById(id);
		if (found.isPresent()) {
			userRepo.delete(found.get());
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Elimina un usuario por su nombre de usuario.
	 *
	 * <ul>
	 *   <li>Devuelve {@code 0}: eliminado correctamente.</li>
	 *   <li>Devuelve {@code 1}: usuario no encontrado.</li>
	 * </ul>
	 *
	 * @param username nombre de usuario del usuario a eliminar
	 * @return código de resultado
	 */
	public int deleteByUsername(String username) {
		Optional<User> found = userRepo.findByUsername(username);
		if (found.isPresent()) {
			userRepo.delete(found.get());
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Actualiza un usuario existente por su ID.
	 * Solo cambia la contraseña si viene no vacía; solo cambia {@code enabled}
	 * si el DTO la envía no nula.
	 *
	 * <ul>
	 *   <li>Devuelve {@code 0}: actualizado correctamente.</li>
	 *   <li>Devuelve {@code 1}: el nuevo nombre de usuario ya está en uso.</li>
	 *   <li>Devuelve {@code 2}: usuario a actualizar no encontrado.</li>
	 *   <li>Devuelve {@code 3}: otros errores.</li>
	 * </ul>
	 *
	 * @param id      ID del usuario a actualizar
	 * @param newData DTO con los nuevos datos
	 * @return código de resultado
	 */
	@Override
	public int updateById(Long id, UserDTO newData) {
		Optional<User> found = userRepo.findById(id);
		if (found.isEmpty()) {
			return 2;
		}

		User temp = found.get();

		// Cambio de username: verificar que no esté en uso por otro
		if (newData.getUsername() != null && !newData.getUsername().isBlank()) {
			Optional<User> existing = userRepo.findByUsername(newData.getUsername());
			if (existing.isPresent() && !existing.get().getId().equals(id)) {
				return 1;
			}
			temp.setUsername(newData.getUsername());
		}

		// Cambio de contraseña: solo si viene no vacía
		if (newData.getPassword() != null && !newData.getPassword().isBlank()) {
			temp.setPassword(passwordEncoder.encode(newData.getPassword()));
		}

		// Cambio de email
		if (newData.getEmail() != null && !newData.getEmail().isBlank()) {
			temp.setEmail(newData.getEmail());
		}

		// Cambio de nombre completo
		if (newData.getFullName() != null) {
			temp.setFullName(newData.getFullName());
		}

		// Cambio de rol
		if (newData.getRole() != null) {
			temp.setRole(newData.getRole());
		}

		// Cambio de enabled: solo si el DTO lo envía explícitamente
		if (newData.getEnabled() != null) {
			temp.setEnabled(newData.getEnabled().booleanValue());
		}

		userRepo.save(temp);
		return 0;
	}


	/**
	 * Obtiene un usuario por su ID.
	 *
	 * @param id ID del usuario
	 * @return DTO del usuario si existe, {@code null} si no
	 */
	public UserDTO getById(Long id) {
		Optional<User> found = userRepo.findById(id);
		return found.map(user -> modelMapper.map(user, UserDTO.class)).orElse(null);
	}

	/**
	 * Obtiene un usuario por su nombre de usuario.
	 *
	 * @param username nombre de usuario
	 * @return DTO del usuario si existe, {@code null} si no
	 */
	public UserDTO getByUsername(String username) {
		Optional<User> found = userRepo.findByUsername(username);
		return found.map(user -> modelMapper.map(user, UserDTO.class)).orElse(null);
	}

	/**
	 * Habilita o deshabilita una cuenta de usuario.
	 *
	 * <ul>
	 *   <li>Devuelve {@code 0}: estado actualizado correctamente.</li>
	 *   <li>Devuelve {@code 1}: usuario no encontrado.</li>
	 * </ul>
	 *
	 * @param id      ID del usuario
	 * @param enabled {@code true} para habilitar, {@code false} para deshabilitar
	 * @return código de resultado
	 */
	public int setEnabled(Long id, boolean enabled) {
		Optional<User> found = userRepo.findById(id);
		if (found.isPresent()) {
			User user = found.get();
			user.setEnabled(enabled);
			userRepo.save(user);
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Verifica si un nombre de usuario ya está registrado en el sistema.
	 *
	 * @param username nombre de usuario a verificar
	 * @return {@code true} si el nombre de usuario ya está en uso
	 */
	public boolean findUsernameAlreadyTaken(String username) {
		return userRepo.findByUsername(username).isPresent();
	}

	/**
	 * Verifica si un nombre de usuario ya está registrado (sobrecarga con entidad).
	 *
	 * @param newUser entidad {@link User} cuyo username se quiere verificar
	 * @return {@code true} si el nombre de usuario ya está en uso
	 */
	public boolean findUsernameAlreadyTaken(User newUser) {
		return userRepo.findByUsername(newUser.getUsername()).isPresent();
	}

	/**
	 * Valida las credenciales de un usuario.
	 *
	 * <ul>
	 *   <li>Devuelve {@code 0}: credenciales válidas.</li>
	 *   <li>Devuelve {@code 1}: credenciales inválidas.</li>
	 * </ul>
	 *
	 * @param username nombre de usuario
	 * @param password contraseña sin encriptar
	 * @return código de resultado
	 */
	public int validateCredentials(String username, String password) {
		Optional<User> userOpt = userRepo.findByUsername(username);
		if (userOpt.isPresent()) {
			User user = userOpt.get();
			if (passwordEncoder.matches(password, user.getPassword())) {
				return 0;
			}
		}
		return 1;
	}
	/**
	 * Valida que la contraseña cumpla los requisitos mínimos de seguridad.
	 *
	 * @param password contraseña a validar
	 * @throws PasswordNotValidException si la contraseña no cumple los requisitos
	 */
	private void validatePassword(String password) throws PasswordNotValidException {
		if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
			throw new PasswordNotValidException();
		}
	}

}
