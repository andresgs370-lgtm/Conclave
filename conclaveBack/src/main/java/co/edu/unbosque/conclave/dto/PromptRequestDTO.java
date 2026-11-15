package co.edu.unbosque.conclave.dto;

import co.edu.unbosque.conclave.model.MediaFormat;
import co.edu.unbosque.conclave.model.OutputType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * DTO de entrada para enviar un prompt a OpenRouter.
 *
 * <p>OpenRouter es el único proveedor de IA de la plataforma y se usa siempre,
 * por lo que no es necesario especificarlo en el request.</p>
 */
public class PromptRequestDTO {
	/** Texto del prompt que se enviará a OpenRouter. */
	@NotBlank(message = "El texto del prompt no puede estar vacío")
	private String promptText;

	/** Tipo de salida deseado (texto, imagen, vídeo, audio). */
	@NotNull(message = "El tipo de salida es obligatorio")
	private OutputType outputType;

	/** Formato de archivo deseado; debe ser compatible con {@code outputType}. */
	private MediaFormat format;

	/** Modelo de OpenRouter a usar (p.ej. {@code "openai/gpt-4o"}, {@code "anthropic/claude-3.5-sonnet"}). */
	private String model;
	
	/** Constructor por defecto. */
	public PromptRequestDTO() {
	}

	/**
	 * Constructor completo.
	 *
	 * @param promptText texto del prompt
	 * @param outputType tipo de salida
	 * @param format     formato de archivo
	 * @param model      modelo de OpenRouter a usar
	 */
	public PromptRequestDTO(String promptText, OutputType outputType,
			MediaFormat format, String model) {
		this.promptText = promptText;
		this.outputType = outputType;
		this.format = format;
		this.model = model;
	}

	public String getPromptText() {
		return promptText;
	}

	public void setPromptText(String promptText) {
		this.promptText = promptText;
	}

	public OutputType getOutputType() {
		return outputType;
	}

	public void setOutputType(OutputType outputType) {
		this.outputType = outputType;
	}

	public MediaFormat getFormat() {
		return format;
	}

	public void setFormat(MediaFormat format) {
		this.format = format;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public int hashCode() {
		return Objects.hash(format, model, outputType, promptText);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PromptRequestDTO other = (PromptRequestDTO) obj;
		return format == other.format && Objects.equals(model, other.model) && outputType == other.outputType
				&& Objects.equals(promptText, other.promptText);
	}

	@Override
	public String toString() {
		return "PromptRequestDTO [promptText=" + promptText + ", outputType=" + outputType + ", format=" + format
				+ ", model=" + model + "]";
	}
	
	
}
