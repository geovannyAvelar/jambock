package dev.avelar.jambock.reports;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * {@link OutputRenderer} implementation that converts HTML to PDF using
 * <a href="https://flyingsaucerproject.github.io/flyingsaucer/">Flying Saucer</a>.
 *
 * <p>This is the default renderer used by {@link ReportEngine}.
 */
public class PdfOutputRenderer implements OutputRenderer {

    /**
     * Converts the supplied HTML string into a PDF document.
     *
     * @param html the fully-rendered XHTML string
     * @return the PDF content as a byte array
     * @throws ReportGenerationException if the HTML-to-PDF conversion fails
     */
    @Override
    public byte[] render(String html) throws ReportGenerationException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(out);
            out.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new ReportGenerationException("Failed to convert HTML to PDF: " + e.getMessage(), e);
        }
    }
}

