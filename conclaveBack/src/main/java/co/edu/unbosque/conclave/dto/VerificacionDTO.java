package co.edu.unbosque.conclave.dto;

import java.util.Objects;

/**
 * DTO para verificacion de correo al registrarse y para solicitar reset.
 *
 * <p>Se usa en dos endpoints:</p>
 * <ul>
 *   <li>Verificar registro: requiere {@code email} y {@code codigo}.</li>
 *   <li>Solicitar reset: requiere solo {@code email}.</li>
 * </ul>
 */
public class VerificacionDTO {

    /** Correo electronico del usuario. */
    private String email;

    /** Codigo de 6 digitos enviado al correo. */
    private String codigo;

    /** Constructor por defecto. */
    public VerificacionDTO() {
    }

    /** @return correo electronico del usuario */
    public String getEmail() {
        return email;
    }

    /** @param email correo a establecer */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return codigo de verificacion */
    public String getCodigo() {
        return codigo;
    }

    /** @param codigo codigo a establecer */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo, email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        VerificacionDTO other = (VerificacionDTO) obj;
        return Objects.equals(codigo, other.codigo)
                && Objects.equals(email, other.email);
    }

    @Override
    public String toString() {
        return "VerificacionDTO [email=" + email + ", codigo=" + codigo + "]";
    }
}
