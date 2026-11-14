package co.edu.unbosque.conclave.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Formatos de archivo soportados por cada tipo de salida.
 * Cada constante declara su OutputType, mime-type y extensión.
 */
public enum MediaFormat {

    // IMAGE
    PNG(OutputType.IMAGE, "image/png", "png"),
    JPEG(OutputType.IMAGE, "image/jpeg", "jpeg"),
    WEBP(OutputType.IMAGE, "image/webp", "webp"),

    // VIDEO 
    MP4(OutputType.VIDEO, "video/mp4", "mp4"),
    WEBM(OutputType.VIDEO, "video/webm", "webm"),

    //AUDIO 
    MP3(OutputType.AUDIO, "audio/mpeg", "mp3"),
    WAV(OutputType.AUDIO, "audio/wav", "wav"),
    OGG(OutputType.AUDIO, "audio/ogg", "ogg");

    private final OutputType outputType;
    private final String mimeType;
    private final String extension;

    MediaFormat(OutputType outputType, String mimeType, String extension) {
        this.outputType = outputType;
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }

    /**
     * Devuelve todos los formatos compatibles con un OutputType dado.
     */
    public static List<MediaFormat> getByOutputType(OutputType type) {
        return Arrays.stream(values())
                .filter(f -> f.outputType == type)
                .collect(Collectors.toList());
    }
}
