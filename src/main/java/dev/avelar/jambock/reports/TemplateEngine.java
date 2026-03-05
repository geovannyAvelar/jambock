package dev.avelar.jambock.reports;

import java.util.Map;

/**
 * Strategy interface for template engines.
 * Implementations are responsible for processing a template with a given data model
 * and returning the resulting HTML string.
 *
 * <p>Built-in implementations are provided for FreeMarker ({@link dev.avelar.jambock.reports.FreemarkerTemplateEngine})
 * and Thymeleaf ({@link dev.avelar.jambock.reports.ThymeleafTemplateEngine}).
 */
public interface TemplateEngine {

    /**
     * Processes the given template with the supplied data model and returns the rendered HTML.
     *
     * @param templateName the name / path of the template to process (relative to the template root)
     * @param data         the data model to expose to the template
     * @return the rendered HTML as a {@link String}
     * @throws ReportGenerationException if the template cannot be found or processed
     */
    String processTemplate(String templateName, Map<String, Object> data) throws ReportGenerationException;
}


