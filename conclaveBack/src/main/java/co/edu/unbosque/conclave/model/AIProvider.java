package co.edu.unbosque.conclave.model;

/**
 * Proveedores externos utilizados para generar contenido multimedia.
 */
public enum AIProvider {
    OPENROUTER("OpenRouter (legacy)"),
    GEMINI("Google Gemini"),
    CLOUDFLARE("Cloudflare Workers AI (Images)"),
    JSON2VIDEO("JSON2Video (legacy)"),
    AGNES_AI("Agnes AI (Video Generation)"),
    FAL_AI("fal.ai (Video Generation)"),
    KINOVI_AI("Kinovi AI (Video Generation)"),
    REPLICATE("Replicate (Video Generation)"),
    HUGGINGFACE("Hugging Face (Video Generation)");

    private final String displayName;
    /**
     * Crea un proveedor con el nombre que se muestra en la interfaz.
     *
     * @param displayName nombre visible del proveedor
     */
    AIProvider(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Obtiene el nombre visible del proveedor.
     *
     * @return nombre del proveedor
     */
    public String getDisplayName() {
        return displayName;
    }
}
