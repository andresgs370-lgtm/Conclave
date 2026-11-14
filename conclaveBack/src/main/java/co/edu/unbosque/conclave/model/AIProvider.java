package co.edu.unbosque.conclave.model;

/**
 * Proveedor de IA utilizado por la plataforma.
 *
 * <p>OpenRouter es una API unificada que da acceso a múltiples modelos de IA
 * (GPT, Claude, Gemini, Llama, etc.) a través de un único endpoint y una única API key.</p>
 */
public enum AIProvider {

	OPENROUTER("OpenRouter");

	private final String displayName;

	AIProvider(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
