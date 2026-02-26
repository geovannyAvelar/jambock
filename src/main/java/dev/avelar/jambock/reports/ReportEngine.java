package dev.avelar.jambock.reports;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * Main report engine that uses Freemarker for templating and Flying Saucer for PDF generation.
 * This class coordinates the template processing and PDF conversion.
 */
public class ReportEngine {

    private static final Logger logger = LoggerFactory.getLogger(ReportEngine.class);

    private final Configuration freemarkerConfig;

    /**
     * Creates a new ReportEngine with default configuration.
     * Templates are loaded from the classpath under /templates directory.
     */
    public ReportEngine() {
        this(createDefaultConfiguration());
    }

    /**
     * Creates a new ReportEngine with custom Freemarker configuration.
     *
     * @param freemarkerConfig the Freemarker configuration to use
     */
    public ReportEngine(Configuration freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    /**
     * Creates a default Freemarker configuration that loads templates from classpath.
     *
     * @return configured Freemarker Configuration instance
     */
    private static Configuration createDefaultConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(ReportEngine.class, "/templates");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setLocale(Locale.US);
        return cfg;
    }

    /**
     * Generates a PDF report from a Freemarker template.
     *
     * @param templateName the name of the template file (relative to template directory)
     * @param data the data model to be used in the template
     * @param outputStream the output stream where the PDF will be written
     * @throws ReportGenerationException if there's an error generating the report
     */
    public void generateReport(String templateName, Map<String, Object> data, OutputStream outputStream)
            throws ReportGenerationException {
        try {
            logger.info("Generating report using template: {}", templateName);

            // Step 1: Process the Freemarker template to generate HTML
            String html = processTemplate(templateName, data);

            logger.debug("HTML generated, converting to PDF...");

            // Step 2: Convert HTML to PDF using Flying Saucer
            convertHtmlToPdf(html, outputStream);

            logger.info("Report generated successfully");

        } catch (Exception e) {
            logger.error("Error generating report", e);
            throw new ReportGenerationException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a PDF report and saves it to a file.
     *
     * @param templateName the name of the template file
     * @param data the data model to be used in the template
     * @param outputFile the output file where the PDF will be saved
     * @throws ReportGenerationException if there's an error generating the report
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
     * @param data the data model to be used in the template
     * @return the PDF content as a byte array
     * @throws ReportGenerationException if there's an error generating the report
     */
    public byte[] generateReportAsBytes(String templateName, Map<String, Object> data)
            throws ReportGenerationException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generateReport(templateName, data, baos);
        return baos.toByteArray();
    }

    /**
     * Processes a Freemarker template with the given data model.
     *
     * @param templateName the name of the template file
     * @param data the data model
     * @return the processed HTML as a string
     * @throws IOException if template cannot be loaded
     * @throws TemplateException if there's an error processing the template
     */
    private String processTemplate(String templateName, Map<String, Object> data)
            throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(data, writer);
        return writer.toString();
    }

    /**
     * Converts HTML content to PDF using Flying Saucer.
     *
     * @param html the HTML content to convert
     * @param outputStream the output stream where the PDF will be written
     * @throws IOException if there's an error writing to the output stream
     */
    private void convertHtmlToPdf(String html, OutputStream outputStream) throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.flush();
    }

    /**
     * Gets the Freemarker configuration used by this engine.
     *
     * @return the Freemarker configuration
     */
    public Configuration getFreemarkerConfig() {
        return freemarkerConfig;
    }
}

