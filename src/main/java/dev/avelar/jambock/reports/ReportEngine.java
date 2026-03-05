package dev.avelar.jambock.reports;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main report engine that converts a template (processed by a {@link TemplateEngine}) into a PDF
 * using <a href="https://flyingsaucerproject.github.io/flyingsaucer/">Flying Saucer</a>.
 *
 * <p>The template processing strategy is fully polymorphic: any {@link TemplateEngine}
 * implementation can be supplied at construction time. Two built-in implementations are provided:
 * <ul>
 *   <li>{@link FreemarkerTemplateEngine} — backed by Apache FreeMarker (default)</li>
 *   <li>{@link ThymeleafTemplateEngine} — backed by Thymeleaf</li>
 * </ul>
 *
 * <p><b>Backward-compatible usage (FreeMarker, default):</b>
 * <pre>{@code
 * ReportEngine engine = new ReportEngine();
 * }</pre>
 *
 * <p><b>Using Thymeleaf:</b>
 * <pre>{@code
 * ReportEngine engine = new ReportEngine(new ThymeleafTemplateEngine());
 * }</pre>
 */
public class ReportEngine {

    private static final Logger logger = Logger.getLogger(ReportEngine.class.getName());

    private final TemplateEngine templateEngine;

    /**
     * Creates a new {@code ReportEngine} that uses the default {@link FreemarkerTemplateEngine}.
     * Templates are loaded from the classpath under {@code /templates}.
     */
    public ReportEngine() {
        this(new FreemarkerTemplateEngine());
    }

    /**
     * Creates a new {@code ReportEngine} with a custom FreeMarker configuration.
     * This constructor is kept for backward compatibility.
     *
     * @param freemarkerConfig the FreeMarker {@link freemarker.template.Configuration} to use
     */
    public ReportEngine(freemarker.template.Configuration freemarkerConfig) {
        this(new FreemarkerTemplateEngine(freemarkerConfig));
    }

    /**
     * Creates a new {@code ReportEngine} that delegates template processing to the given
     * {@link TemplateEngine} strategy.
     *
     * @param templateEngine the template engine to use (FreeMarker, Thymeleaf, or custom)
     */
    public ReportEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Generates a PDF report from a template.
     *
     * @param templateName the name of the template file (relative to the template directory)
     * @param data         the data model to be used in the template
     * @param outputStream the output stream where the PDF will be written
     * @throws ReportGenerationException if there is an error generating the report
     */
    public void generateReport(String templateName, Map<String, Object> data, OutputStream outputStream)
            throws ReportGenerationException {
        try {
            logger.info("Generating report using template: " + templateName);

            // Step 1: process the template to obtain HTML
            String html = templateEngine.processTemplate(templateName, data);

            logger.fine("HTML generated, converting to PDF...");

            // Step 2: convert HTML to PDF using Flying Saucer
            convertHtmlToPdf(html, outputStream);

            logger.info("Report generated successfully");

        } catch (ReportGenerationException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating report", e);
            throw new ReportGenerationException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a PDF report and saves it to a file.
     *
     * @param templateName the name of the template file
     * @param data         the data model to be used in the template
     * @param outputFile   the output file where the PDF will be saved
     * @throws ReportGenerationException if there is an error generating the report
     */
    public void generateReport(String templateName, Map<String, Object> data, File outputFile)
            throws ReportGenerationException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            generateReport(templateName, data, fos);
        } catch (IOException e) {
            throw new ReportGenerationException("Failed to write report to file: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a PDF report and returns it as a byte array.
     *
     * @param templateName the name of the template file
     * @param data         the data model to be used in the template
     * @return the PDF content as a byte array
     * @throws ReportGenerationException if there is an error generating the report
     */
    public byte[] generateReportAsBytes(String templateName, Map<String, Object> data)
            throws ReportGenerationException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generateReport(templateName, data, baos);
        return baos.toByteArray();
    }

    /**
     * Returns the {@link TemplateEngine} strategy used by this engine.
     *
     * @return the current template engine
     */
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    /**
     * Converts HTML content to PDF using Flying Saucer.
     *
     * @param html         the HTML content to convert
     * @param outputStream the output stream where the PDF will be written
     * @throws IOException if there is an error writing to the output stream
     */
    private void convertHtmlToPdf(String html, OutputStream outputStream) throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.flush();
    }
}
