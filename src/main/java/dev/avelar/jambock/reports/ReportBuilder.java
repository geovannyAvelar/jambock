package dev.avelar.jambock.reports;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for creating and generating reports.
 * Provides a fluent API for setting template, data, output destination, page settings,
 * and output format.
 */
public class ReportBuilder {

    private final ReportEngine engine;
    private String templateName;
    private final Map<String, Object> data;
    private PageOrientation orientation = PageOrientation.PORTRAIT;
    private PageSize pageSize = PageSize.A4;
    private OutputRenderer outputRenderer;

    /**
     * Creates a new ReportBuilder with the given engine.
     *
     * @param engine the report engine to use
     */
    public ReportBuilder(ReportEngine engine) {
        this.engine = engine;
        this.data = new HashMap<>();
    }

    /**
     * Sets the template to use for the report.
     *
     * @param templateName the name of the template file
     * @return this builder for method chaining
     */
    public ReportBuilder withTemplate(String templateName) {
        this.templateName = templateName;
        return this;
    }

    /**
     * Adds a single data entry to the data model.
     *
     * @param key the key for the data
     * @param value the value for the data
     * @return this builder for method chaining
     */
    public ReportBuilder withData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * Adds multiple data entries to the data model.
     *
     * @param data the data map to add
     * @return this builder for method chaining
     */
    public ReportBuilder withData(Map<String, Object> data) {
        this.data.putAll(data);
        return this;
    }

    /**
     * Clears all data from the data model.
     *
     * @return this builder for method chaining
     */
    public ReportBuilder clearData() {
        this.data.clear();
        return this;
    }

    /**
     * Sets the page orientation for the PDF.
     *
     * @param orientation the page orientation (PORTRAIT or LANDSCAPE)
     * @return this builder for method chaining
     */
    public ReportBuilder withOrientation(PageOrientation orientation) {
        this.orientation = orientation;
        this.data.put("pageOrientation", orientation.getValue());
        return this;
    }

    /**
     * Sets the page size for the PDF.
     *
     * @param pageSize the page size (A4, Letter, Legal, A3, A5)
     * @return this builder for method chaining
     */
    public ReportBuilder withPageSize(PageSize pageSize) {
        this.pageSize = pageSize;
        this.data.put("pageSize", pageSize.getValue());
        return this;
    }

    /**
     * Convenience method to set page orientation to LANDSCAPE.
     *
     * @return this builder for method chaining
     */
    public ReportBuilder landscape() {
        return withOrientation(PageOrientation.LANDSCAPE);
    }

    /**
     * Convenience method to set page orientation to PORTRAIT.
     *
     * @return this builder for method chaining
     */
    public ReportBuilder portrait() {
        return withOrientation(PageOrientation.PORTRAIT);
    }

    /**
     * Overrides the {@link OutputRenderer} for this report, ignoring the one configured on the
     * {@link ReportEngine}. Useful when a single engine is shared but different reports need
     * different output formats.
     *
     * @param outputRenderer the renderer to use (e.g. {@link PdfOutputRenderer}, {@link DocxOutputRenderer})
     * @return this builder for method chaining
     */
    public ReportBuilder withOutputRenderer(OutputRenderer outputRenderer) {
        this.outputRenderer = outputRenderer;
        return this;
    }

    /**
     * Generates the report and writes it to the specified output stream.
     *
     * @param outputStream the output stream where the document will be written
     * @throws ReportGenerationException if there's an error generating the report
     */
    public void generateTo(OutputStream outputStream) throws ReportGenerationException {
        validateState();
        resolvedEngine().generateReport(templateName, data, outputStream);
    }

    /**
     * Generates the report and saves it to the specified file.
     *
     * @param outputFile the output file where the document will be saved
     * @throws ReportGenerationException if there's an error generating the report
     */
    public void generateTo(File outputFile) throws ReportGenerationException {
        validateState();
        resolvedEngine().generateReport(templateName, data, outputFile);
    }

    /**
     * Generates the report and returns it as a byte array.
     *
     * @return the document content as a byte array
     * @throws ReportGenerationException if there's an error generating the report
     */
    public byte[] generateAsBytes() throws ReportGenerationException {
        validateState();
        return resolvedEngine().generateReportAsBytes(templateName, data);
    }

    /**
     * Convenience method: generates the report as a DOCX byte array using {@link DocxOutputRenderer},
     * regardless of the renderer configured on the engine.
     *
     * @return the DOCX content as a byte array
     * @throws ReportGenerationException if there's an error generating the report
     */
    public byte[] generateAsDocx() throws ReportGenerationException {
        return withOutputRenderer(new DocxOutputRenderer()).generateAsBytes();
    }

    /**
     * Validates that the builder is in a valid state for report generation.
     *
     * @throws IllegalStateException if the builder is not in a valid state
     */
    private void validateState() {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalStateException("Template name must be set before generating report");
        }
    }

    /**
     * Returns either a one-shot engine with the overridden renderer, or the original engine
     * if no renderer override has been specified.
     */
    private ReportEngine resolvedEngine() {
        if (outputRenderer != null) {
            return new ReportEngine(engine.getTemplateEngine(), outputRenderer);
        }
        return engine;
    }
}


