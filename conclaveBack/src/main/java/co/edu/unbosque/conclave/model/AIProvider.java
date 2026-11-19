package co.edu.unbosque.conclave.model;

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
    AIProvider(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
