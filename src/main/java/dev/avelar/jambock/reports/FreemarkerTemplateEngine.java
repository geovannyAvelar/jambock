package dev.avelar.jambock.reports;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * {@link TemplateEngine} implementation backed by <a href="https://freemarker.apache.org/">Apache FreeMarker</a>.
 *
 * <p>Templates are loaded from the classpath under the {@code /templates} directory by default.
 * A custom {@link Configuration} can be supplied via the
 * {@link #FreemarkerTemplateEngine(Configuration)} constructor.
 */
public class FreemarkerTemplateEngine implements TemplateEngine {

    private final Configuration freemarkerConfig;

    /**
     * Creates a new {@code FreemarkerTemplateEngine} with the default FreeMarker configuration.
     * Templates are loaded from {@code /templates} on the classpath.
     */
    public FreemarkerTemplateEngine() {
        this(createDefaultConfiguration());
    }

    /**
     * Creates a new {@code FreemarkerTemplateEngine} with a custom FreeMarker configuration.
     *
     * @param freemarkerConfig the FreeMarker {@link Configuration} to use
     */
    public FreemarkerTemplateEngine(Configuration freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ReportGenerationException wrapping any {@link IOException} or {@link TemplateException}
     *                                   thrown during template processing
     */
    @Override
    public String processTemplate(String templateName, Map<String, Object> data) throws ReportGenerationException {
        try {
            Template template = freemarkerConfig.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(data, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new ReportGenerationException(
                    "FreeMarker failed to process template '" + templateName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Returns the underlying FreeMarker {@link Configuration}.
     *
     * @return the FreeMarker configuration
     */
    public Configuration getFreemarkerConfig() {
        return freemarkerConfig;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static Configuration createDefaultConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(FreemarkerTemplateEngine.class, "/templates");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setLocale(Locale.US);
        return cfg;
    }
}

