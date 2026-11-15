package co.edu.unbosque.conclave.service;

import java.util.List;

/**
 * Interfaz genérica que define las operaciones CRUD básicas
 * (Crear, Leer, Actualizar, Eliminar).
 *
 * @param <T> tipo de DTO sobre el que se realizarán las operaciones
 */
public interface CRUDOperation<T> {

	/**
	 * Crea una nueva entidad.
	 *
	 * @param data datos de la entidad a crear
	 * @return código de resultado de la operación
	 */
	public int create(T data);

	/**
	 * Obtiene todas las entidades.
	 *
	 * @return lista de todas las entidades como DTOs
	 */
	public List<T> getAll();

	/**
	 * Elimina una entidad por su identificador.
	 *
	 * @param id identificador de la entidad a eliminar
	 * @return código de resultado de la operación
	 */
	public int deleteById(Long id);

	/**
	 * Actualiza una entidad existente por su identificador.
	 *
	 * @param id      identificador de la entidad a actualizar
	 * @param newData nuevos datos para la entidad
	 * @return código de resultado de la operación
	 */
	public int updateById(Long id, T newData);

	/**
	 * Cuenta el número total de entidades.
	 *
	 * @return número total de entidades
	 */
	public long count();

	/**
	 * Verifica si existe una entidad con el identificador especificado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe la entidad
	 */
	public boolean exist(Long id);

}
