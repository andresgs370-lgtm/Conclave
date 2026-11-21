package co.edu.unbosque.conclave.dto;

import java.util.Objects;

/**
 * DTO para confirmar el restablecimiento de contrasena.
 *
 * <p>Se usa en el endpoint {@code POST /auth/confirmar-reset}
 * que requiere el correo, el codigo recibido y la nueva contrasena.</p>
 */
public class ResetPasswordDTO {

    /** Correo electronico del usuario. */
    private String email;

    /** Codigo de 6 digitos recibido por correo. */
    private String codigo;

    /** Nueva contrasena en texto plano. */
    private String nuevaPassword;

    /** Constructor por defecto. */
    public ResetPasswordDTO() {
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

    /** @return nueva contrasena */
    public String getNuevaPassword() {
        return nuevaPassword;
    }

    /** @param nuevaPassword nueva contrasena a establecer */
    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo, email, nuevaPassword);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ResetPasswordDTO other = (ResetPasswordDTO) obj;
        return Objects.equals(codigo, other.codigo)
                && Objects.equals(email, other.email)
                && Objects.equals(nuevaPassword, other.nuevaPassword);
    }

    @Override
    public String toString() {
        return "ResetPasswordDTO [email=" + email + ", codigo=" + codigo + "]";
    }
}
