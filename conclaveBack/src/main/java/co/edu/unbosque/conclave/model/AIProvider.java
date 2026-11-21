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
    HUGGINGFACE("Hugging Face (Video Generation)"),
    /** Modelo MiniMax H3 FlashGen de texto a video alojado en Hugging Face. */
    HF_MINIMAX_H3("Hugging Face · MiniMax H3 FlashGen"),
    /** Modelo MiniMax H3 Turbo de texto a video con audio alojado en Hugging Face. */
    HF_MINIMAX_TURBO("Hugging Face · MiniMax H3 Turbo"),
    /** Modelo Omni Video de texto a video alojado en Hugging Face. */
    HF_OMNI_VIDEO("Hugging Face · Omni Video"),
    /** Modelo ModelScope de texto a video alojado en Hugging Face. */
    HF_MODELSCOPE("Hugging Face · ModelScope Text-to-Video");

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