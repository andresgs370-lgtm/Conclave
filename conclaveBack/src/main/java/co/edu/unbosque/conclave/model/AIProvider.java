package co.edu.unbosque.conclave.model;

/**
 * Proveedores externos utilizados para generar contenido multimedia.
 */
public enum AIProvider {
    /** Generación de imágenes con varios modelos a través de OpenRouter. */
    OPENROUTER("OpenRouter (Imágenes)"),
    /** Generación de imágenes y voz con Google Gemini. */
    GEMINI("Google Gemini (Imágenes y sonido)"),
    /** Generación de imágenes con FLUX y voz con Deepgram Aura en Cloudflare Workers AI. */
    CLOUDFLARE("Cloudflare Workers AI (Imágenes y sonido)"),
    /** Modelo MiniMax H3 FlashGen de texto a video alojado en Hugging Face. */
    HF_MINIMAX_H3("Hugging Face · MiniMax H3 FlashGen"),
    /** Modelo MiniMax H3 Turbo de texto a video con audio alojado en Hugging Face. */
    HF_MINIMAX_TURBO("Hugging Face · MiniMax H3 Turbo"),
    /** Modelo Omni Video de texto a video alojado en Hugging Face. */
    HF_OMNI_VIDEO("Hugging Face · Omni Video"),
    /** Modelo ModelScope de texto a video alojado en Hugging Face. */
    HF_MODELSCOPE("Hugging Face · ModelScope Text-to-Video"),
    /** Generación gratuita de imágenes con Pollinations.ai; la API key es opcional. */
    POLLINATIONS("Pollinations.ai (Imágenes, key opcional)"),
    /** Generación de imágenes con FLUX.1 schnell gratuito de Together AI. */
    TOGETHER_AI("Together AI (Imágenes)"),
    /** Generación de voz con el plan gratuito de ElevenLabs. */
    ELEVENLABS("ElevenLabs (Sonido)");

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