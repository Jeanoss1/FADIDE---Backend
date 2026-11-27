package pe.com.fadide.sisco.model;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Lista blanca de archivos que se aceptan como evidencia. Cada tipo define su extensión,
 * el Content-Type con el que se sirve y la "firma" (primeros bytes) que debe tener el
 * contenido real. Así no basta con renombrar un archivo para colarlo: un .exe renombrado
 * a .pdf se rechaza porque no empieza con %PDF.
 * No se aceptan HTML, SVG, scripts ni ejecutables.
 */
public enum TipoArchivo {

    JPG("jpg", "image/jpeg", Categoria.IMAGEN, b -> empieza(b, 0xFF, 0xD8, 0xFF)),
    JPEG("jpeg", "image/jpeg", Categoria.IMAGEN, b -> empieza(b, 0xFF, 0xD8, 0xFF)),
    PNG("png", "image/png", Categoria.IMAGEN, b -> empieza(b, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)),
    GIF("gif", "image/gif", Categoria.IMAGEN, b -> texto(b, 0, "GIF87a") || texto(b, 0, "GIF89a")),
    WEBP("webp", "image/webp", Categoria.IMAGEN, b -> texto(b, 0, "RIFF") && texto(b, 8, "WEBP")),
    PDF("pdf", "application/pdf", Categoria.PDF, b -> texto(b, 0, "%PDF-")),
    // Office moderno (OOXML): internamente son archivos ZIP
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", Categoria.WORD, TipoArchivo::esZip),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Categoria.EXCEL, TipoArchivo::esZip),
    PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", Categoria.POWERPOINT, TipoArchivo::esZip),
    // Office 97-2003 (formato OLE2)
    DOC("doc", "application/msword", Categoria.WORD, TipoArchivo::esOle2),
    XLS("xls", "application/vnd.ms-excel", Categoria.EXCEL, TipoArchivo::esOle2),
    PPT("ppt", "application/vnd.ms-powerpoint", Categoria.POWERPOINT, TipoArchivo::esOle2),
    // Planos de AutoCAD (habituales en obra): empiezan con "AC10"
    DWG("dwg", "application/acad", Categoria.PLANO, b -> texto(b, 0, "AC10")),
    TXT("txt", "text/plain; charset=UTF-8", Categoria.TEXTO, TipoArchivo::esTexto),
    CSV("csv", "text/csv; charset=UTF-8", Categoria.TEXTO, TipoArchivo::esTexto);

    public enum Categoria { IMAGEN, PDF, WORD, EXCEL, POWERPOINT, PLANO, TEXTO }

    private final String extension;
    private final String contentType;
    private final Categoria categoria;
    private final Predicate<byte[]> firma;

    TipoArchivo(String extension, String contentType, Categoria categoria, Predicate<byte[]> firma) {
        this.extension = extension;
        this.contentType = contentType;
        this.categoria = categoria;
        this.firma = firma;
    }

    public String extension() {
        return extension;
    }

    public String contentType() {
        return contentType;
    }

    public Categoria categoria() {
        return categoria;
    }

    /** Las imágenes y los PDF se pueden ver en el navegador; el resto se descarga. */
    public boolean visibleEnNavegador() {
        return categoria == Categoria.IMAGEN || categoria == Categoria.PDF;
    }

    public boolean coincideFirma(byte[] cabecera) {
        return firma.test(cabecera);
    }

    public static Optional<TipoArchivo> porExtension(String extension) {
        String ext = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(t -> t.extension.equals(ext)).findFirst();
    }

    public static Optional<TipoArchivo> porContentType(String contentType) {
        return Arrays.stream(values()).filter(t -> t.contentType.equals(contentType)).findFirst();
    }

    public static String extensionesPermitidas() {
        return String.join(", ", Arrays.stream(values()).map(t -> t.extension).toList());
    }

    // ---------- verificación de firmas ----------

    private static boolean empieza(byte[] b, int... firma) {
        if (b.length < firma.length) return false;
        for (int i = 0; i < firma.length; i++) {
            if ((b[i] & 0xFF) != firma[i]) return false;
        }
        return true;
    }

    private static boolean texto(byte[] b, int desde, String esperado) {
        byte[] e = esperado.getBytes(StandardCharsets.US_ASCII);
        if (b.length < desde + e.length) return false;
        for (int i = 0; i < e.length; i++) {
            if (b[desde + i] != e[i]) return false;
        }
        return true;
    }

    private static boolean esZip(byte[] b) {
        return empieza(b, 0x50, 0x4B, 0x03, 0x04);
    }

    private static boolean esOle2(byte[] b) {
        return empieza(b, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1);
    }

    // Texto plano: sin bytes nulos en la cabecera leída
    private static boolean esTexto(byte[] b) {
        for (byte x : b) {
            if (x == 0) return false;
        }
        return true;
    }
}
