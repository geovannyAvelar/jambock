package dev.avelar.jambock.reports;

/**
 * Strategy interface for converting a rendered HTML string into a final output format
 * (e.g. PDF, DOCX, HTML).
 *
 * <p>Built-in implementations:
 * <ul>
 *   <li>{@link PdfOutputRenderer} — converts HTML to PDF via Flying Saucer (default)</li>
 *   <li>DocxOutputRenderer — converts HTML to DOCX via Apache POI + Jsoup</li>
 * </ul>
 */
public interface OutputRenderer {

    /**
     * Renders the given HTML string into the target output format.
     *
     * @param html the fully-rendered HTML string produced by a {@link TemplateEngine}
     * @return the output document as a byte array
     * @throws ReportGenerationException if rendering fails
     */
    byte[] render(String html) throws ReportGenerationException;
}


