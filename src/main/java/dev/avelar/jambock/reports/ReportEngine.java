package dev.avelar.jambock.reports;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main report engine that converts a template (processed by a {@link TemplateEngine}) into a
 * document using a pluggable {@link OutputRenderer} strategy.
 *
 * <p>Both the template engine and the output renderer are fully polymorphic:
 * <ul>
 *   <li>Template engines: {@link FreemarkerTemplateEngine} (default), {@link ThymeleafTemplateEngine}</li>
 *   <li>Output renderers: {@link PdfOutputRenderer} (default), {@link DocxOutputRenderer}</li>
 * </ul>
 *
 * <p><b>PDF output (default, FreeMarker):</b>
 * <pre>{@code
 * ReportEngine engine = new ReportEngine();
 * }</pre>
 *
 * <p><b>DOCX output with Thymeleaf:</b>
 * <pre>{@code
 * ReportEngine engine = new ReportEngine(new ThymeleafTemplateEngine(), new DocxOutputRenderer());
 * }</pre>
 */
public class ReportEngine {

    private static final Logger logger = Logger.getLogger(ReportEngine.class.getName());

    private final TemplateEngine templateEngine;
    private final OutputRenderer outputRenderer;

    /**
     * Creates a new {@code ReportEngine} with the default {@link FreemarkerTemplateEngine}
     * and {@link PdfOutputRenderer}.
     */
    public ReportEngine() {
        this(new FreemarkerTemplateEngine(), new PdfOutputRenderer());
    }

    /**
     * Creates a new {@code ReportEngine} with a custom FreeMarker configuration and PDF output.
     * Kept for backward compatibility.
     *
     * @param freemarkerConfig the FreeMarker {@link freemarker.template.Configuration} to use
     */
    public ReportEngine(freemarker.template.Configuration freemarkerConfig) {
        this(new FreemarkerTemplateEngine(freemarkerConfig), new PdfOutputRenderer());
    }

    /**
     * Creates a new {@code ReportEngine} with the given {@link TemplateEngine} and the default
     * {@link PdfOutputRenderer}.
     *
     * @param templateEngine the template engine to use (FreeMarker, Thymeleaf, or custom)
     */
    public ReportEngine(TemplateEngine templateEngine) {
        this(templateEngine, new PdfOutputRenderer());
    }

    /**
     * Creates a new {@code ReportEngine} with a custom {@link TemplateEngine} and
     * {@link OutputRenderer}.
     *
     * @param templateEngine the template engine to use
     * @param outputRenderer the output renderer to use (PDF, DOCX, or custom)
     */
    public ReportEngine(TemplateEngine templateEngine, OutputRenderer outputRenderer) {
        this.templateEngine = templateEngine;
        this.outputRenderer = outputRenderer;
    }

    /**
     * Generates a report from a template and writes it to the given output stream.
     * The output format is determined by the configured {@link OutputRenderer}.
     *
     * @param templateName the name of the template file (relative to the template directory)
     * @param data         the data model to be used in the template
     * @param outputStream the output stream where the document will be written
     * @throws ReportGenerationException if there is an error generating the report
     */
    public void generateReport(String templateName, Map<String, Object> data, OutputStream outputStream)
            throws ReportGenerationException {
        try {
            logger.info("Generating report using template: " + templateName);

            String html = templateEngine.processTemplate(templateName, data);
            logger.fine("HTML generated, converting to output format...");

            byte[] output = outputRenderer.render(html);
            outputStream.write(output);
            outputStream.flush();

            logger.info("Report generated successfully");
        } catch (ReportGenerationException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating report", e);
            throw new ReportGenerationException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a report and saves it to a file.
     * The output format is determined by the configured {@link OutputRenderer}.
     *
     * @param templateName the name of the template file
     * @param data         the data model to be used in the template
     * @param outputFile   the output file where the document will be saved
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
     * Generates a report and returns it as a byte array.
     * The output format is determined by the configured {@link OutputRenderer}.
     *
     * @param templateName the name of the template file
     * @param data         the data model to be used in the template
     * @return the document content as a byte array
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
     * Returns the {@link OutputRenderer} strategy used by this engine.
     *
     * @return the current output renderer
     */
    public OutputRenderer getOutputRenderer() {
        return outputRenderer;
    }
}


