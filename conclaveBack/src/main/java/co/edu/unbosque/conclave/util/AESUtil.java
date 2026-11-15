package co.edu.unbosque.conclave.util;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Clase de utilidad para operaciones de cifrado AES y funciones de hash.
 * Proporciona métodos para cifrar y descifrar texto usando AES en modo GCM,
 * así como métodos para generar hashes con varios algoritmos
 * (MD5, SHA-1, SHA-256, SHA-384, SHA-512).
 *
 * <p>Se usa principalmente para cifrar las API keys de los proveedores de IA
 * antes de almacenarlas en la base de datos.</p>
 */
public class AESUtil {

	/** Logger de la clase para registrar errores de cifrado. */
	private static final Logger LOGGER = Logger.getLogger(AESUtil.class.getName());

	/** Algoritmo de cifrado utilizado (AES). */
	private static final String ALGORITMO = "AES";

	/** Modo de cifrado y padding utilizados (AES en modo GCM sin padding). */
	private static final String TIPOCIFRADO = "AES/GCM/NoPadding";

	/** Constructor privado — clase de utilidad, no debe instanciarse. */
	private AESUtil() {
	}

	/**
	 * Cifra un texto utilizando AES en modo GCM.
	 *
	 * @param llave clave de cifrado (debe tener 16 caracteres para AES-128)
	 * @param iv    vector de inicialización para el cifrado
	 * @param texto texto a cifrar
	 * @return texto cifrado en formato Base64, o cadena vacía si ocurre un error
	 */
	public static String encrypt(String llave, String iv, String texto) {
		try {
			Cipher cipher = Cipher.getInstance(TIPOCIFRADO);
			SecretKeySpec secretKeySpec = new SecretKeySpec(
					llave.getBytes(StandardCharsets.UTF_8), ALGORITMO);
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(
					128, iv.getBytes(StandardCharsets.UTF_8));
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
			byte[] encrypted = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
			return new String(encodeBase64(encrypted), StandardCharsets.UTF_8);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeyException | InvalidAlgorithmParameterException
				| IllegalBlockSizeException | BadPaddingException e) {
			LOGGER.log(Level.SEVERE, "Error al cifrar el texto", e);
			return "";
		}
	}

	/**
	 * Descifra un texto cifrado con AES en modo GCM.
	 *
	 * @param llave     clave de cifrado (debe ser la misma usada para cifrar)
	 * @param iv        vector de inicialización (debe ser el mismo usado para cifrar)
	 * @param encrypted texto cifrado en formato Base64
	 * @return texto descifrado, o cadena vacía si ocurre un error
	 */
	public static String decrypt(String llave, String iv, String encrypted) {
		try {
			Cipher cipher = Cipher.getInstance(TIPOCIFRADO);
			SecretKeySpec secretKeySpec = new SecretKeySpec(
					llave.getBytes(StandardCharsets.UTF_8), ALGORITMO);
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(
					128, iv.getBytes(StandardCharsets.UTF_8));
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
			byte[] enc = decodeBase64(encrypted);
			byte[] decrypted = cipher.doFinal(enc);
			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeyException | InvalidAlgorithmParameterException
				| IllegalBlockSizeException | BadPaddingException e) {
			LOGGER.log(Level.SEVERE, "Error al descifrar el texto", e);
			return "";
		}
	}

	/**
	 * Descifra un texto usando una clave y vector de inicialización predeterminados.
	 *
	 * @param encrypted texto cifrado en formato Base64
	 * @return texto descifrado
	 */
	public static String decrypt(String encrypted) {
		String iv = "programacioncomp";
		String key = "llavede16carater";
		return decrypt(key, iv, encrypted);
	}

	/**
	 * Cifra un texto usando una clave y vector de inicialización predeterminados.
	 *
	 * @param plainText texto a cifrar
	 * @return texto cifrado en formato Base64
	 */
	public static String encrypt(String plainText) {
		String iv = "programacioncomp";
		String key = "llavede16carater";
		return encrypt(key, iv, plainText);
	}

	/**
	 * Genera un hash MD5 del contenido proporcionado.
	 *
	 * @param content texto a convertir en hash
	 * @return representación hexadecimal del hash MD5
	 */
	public static String hashingToMD5(String content) {
		return DigestUtils.md5Hex(content);
	}

	/**
	 * Genera un hash SHA-1 del contenido proporcionado.
	 *
	 * @param content texto a convertir en hash
	 * @return representación hexadecimal del hash SHA-1
	 */
	public static String hashingToSHA1(String content) {
		return DigestUtils.sha1Hex(content);
	}

	/**
	 * Genera un hash SHA-256 del contenido proporcionado.
	 *
	 * @param content texto a convertir en hash
	 * @return representación hexadecimal del hash SHA-256
	 */
	public static String hashingToSHA256(String content) {
		return DigestUtils.sha256Hex(content);
	}

	/**
	 * Genera un hash SHA-384 del contenido proporcionado.
	 *
	 * @param content texto a convertir en hash
	 * @return representación hexadecimal del hash SHA-384
	 */
	public static String hashingToSHA384(String content) {
		return DigestUtils.sha384Hex(content);
	}

	/**
	 * Genera un hash SHA-512 del contenido proporcionado.
	 *
	 * @param content texto a convertir en hash
	 * @return representación hexadecimal del hash SHA-512
	 */
	public static String hashingToSHA512(String content) {
		return DigestUtils.sha512Hex(content);
	}
	
}
