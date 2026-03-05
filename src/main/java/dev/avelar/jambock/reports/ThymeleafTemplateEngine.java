package dev.avelar.jambock.reports;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Locale;
import java.util.Map;

/**
 * {@link dev.avelar.jambock.reports.TemplateEngine} implementation backed by
 * <a href="https://www.thymeleaf.org/">Thymeleaf</a>.
 *
 * <p>Templates are loaded from the classpath under the {@code /templates/} directory by default.
 * They are expected to be well-formed XHTML files (suffix {@code .html}).
 *
 * <p>A custom Thymeleaf {@link TemplateEngine} can be supplied via the
 * {@link #ThymeleafTemplateEngine(TemplateEngine)} constructor for advanced configuration
 * (e.g. custom template resolvers, dialect registration, caching policies, etc.).
 *
 * <p><b>Template naming convention:</b> pass the template name <em>without</em> the {@code .html}
 * suffix — the resolver appends it automatically.
 */
public class ThymeleafTemplateEngine implements dev.avelar.jambock.reports.TemplateEngine {

    private final TemplateEngine thymeleafEngine;

    /**
     * Creates a new {@code ThymeleafTemplateEngine} with the default Thymeleaf configuration.
     * Templates are resolved from {@code /templates/} on the classpath with suffix {@code .html}.
     */
    public ThymeleafTemplateEngine() {
        this(createDefaultEngine());
    }

    /**
     * Creates a new {@code ThymeleafTemplateEngine} with a custom Thymeleaf {@link TemplateEngine}.
     *
     * @param thymeleafEngine the pre-configured Thymeleaf engine
     */
    public ThymeleafTemplateEngine(TemplateEngine thymeleafEngine) {
        this.thymeleafEngine = thymeleafEngine;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The {@code templateName} should be the logical template name without the {@code .html}
     * suffix (e.g. {@code "invoice"} resolves to {@code /templates/invoice.html} on the classpath).
     *
     * @throws ReportGenerationException wrapping any runtime exception thrown by Thymeleaf
     */
    @Override
    public String processTemplate(String templateName, Map<String, Object> data) throws ReportGenerationException {
        try {
            Context context = new Context(Locale.US);
            context.setVariables(data);
            return thymeleafEngine.process(templateName, context);
        } catch (Exception e) {
            throw new ReportGenerationException(
                    "Thymeleaf failed to process template '" + templateName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Returns the underlying Thymeleaf {@link TemplateEngine}.
     *
     * @return the Thymeleaf engine
     */
    public TemplateEngine getThymeleafEngine() {
        return thymeleafEngine;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static TemplateEngine createDefaultEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}

