# Jambock Reports Engine

A powerful PDF and DOCX report generation engine supporting **FreeMarker** and **Thymeleaf** templates, with **Flying Saucer** for HTML-to-PDF conversion and **Apache POI** for DOCX output.

## Requirements

- **Java 8+** - Compatible with Java 8 and later versions

## Features

- ✅ Template-based report generation using **FreeMarker** or **Thymeleaf**
- ✅ Pluggable **`TemplateEngine` strategy** — bring your own template engine
- ✅ HTML to PDF conversion with Flying Saucer (based on iText)
- ✅ HTML to **DOCX** conversion with Apache POI
- ✅ Pluggable **`OutputRenderer` strategy** — PDF, DOCX, or custom
- ✅ **Portrait and Landscape page orientations**
- ✅ **Multiple page sizes** (A4, Letter, Legal, A3, A5)
- ✅ Fluent API with builder pattern
- ✅ Support for CSS styling in templates
- ✅ Multiple output options (File, OutputStream, byte array)
- ✅ Pre-built templates (sample report, landscape report, and invoice)
- ✅ CSS `@page` helper via `PageStyleHelper`
- ✅ Comprehensive error handling

## Dependencies

All dependencies are compatible with Java 8:

```kotlin
// FreeMarker template engine
implementation("org.freemarker:freemarker:2.3.32")

// Thymeleaf template engine
implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")

// Flying Saucer for HTML to PDF conversion
implementation("org.xhtmlrenderer:flying-saucer-pdf:9.5.1")

// Apache POI for DOCX output
implementation("org.apache.poi:poi-ooxml:5.4.0")

// Jsoup for HTML parsing (used by DOCX renderer)
implementation("org.jsoup:jsoup:1.18.1")
```

> **Note:** Logging is handled via `java.util.logging` (JUL), which is built into the JDK — no extra logging dependency required. When used with Spring Boot, JUL is automatically bridged to the application's logging system.

## Quick Start

### Basic Usage (FreeMarker — default)

```java
// Create the report engine (uses FreeMarker by default)
ReportEngine engine = new ReportEngine();

// Prepare your data
Map<String, Object> data = new HashMap<>();
data.put("title", "Monthly Sales Report");
data.put("generatedDate", LocalDate.now().toString());

// Generate the report
File outputFile = new File("report.pdf");
engine.generateReport("sample-report.ftl", data, outputFile);
```

### Basic Usage (Thymeleaf)

```java
// Create the report engine backed by Thymeleaf
ReportEngine engine = new ReportEngine(new ThymeleafTemplateEngine());

Map<String, Object> data = new HashMap<>();
data.put("title", "Monthly Sales Report");

// Template name WITHOUT the .html suffix — the resolver appends it automatically
engine.generateReport("sample-report", data, new File("report.pdf"));
```

### Generating a DOCX Document

```java
// PDF output with FreeMarker (default)
ReportEngine pdfEngine = new ReportEngine();

// DOCX output with FreeMarker
ReportEngine docxEngine = new ReportEngine(new FreemarkerTemplateEngine(), new DocxOutputRenderer());

// DOCX output with Thymeleaf
ReportEngine thymeleafDocxEngine = new ReportEngine(new ThymeleafTemplateEngine(), new DocxOutputRenderer());

Map<String, Object> data = new HashMap<>();
data.put("title", "My Report");

// Generate DOCX directly via the engine
docxEngine.generateReport("sample-report.ftl", data, new File("report.docx"));

// Or use the builder convenience method (overrides renderer regardless of engine config)
byte[] docxBytes = new ReportBuilder(pdfEngine)
    .withTemplate("sample-report.ftl")
    .withData(data)
    .generateAsDocx();
```

### Using the Builder Pattern

```java
ReportEngine engine = new ReportEngine();

byte[] pdfBytes = new ReportBuilder(engine)
    .withTemplate("sample-report.ftl")
    .withData("title", "My Report")
    .withData("author", "John Doe")
    .landscape()              // Set to landscape orientation
    .withPageSize(PageSize.A4)
    .withData(dataMap)        // Add multiple entries at once
    .generateAsBytes();
```

## Template Engines

The `TemplateEngine` interface is the central strategy for template processing. You can use the built-in implementations or provide your own.

### FreemarkerTemplateEngine

Backed by [Apache FreeMarker](https://freemarker.apache.org/). Templates are loaded from `/templates` on the classpath by default (`.ftl` files).

```java
// Default configuration — loads from /templates on the classpath
ReportEngine engine = new ReportEngine(new FreemarkerTemplateEngine());

// Custom FreeMarker configuration
Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
cfg.setDirectoryForTemplateLoading(new File("/path/to/templates"));
cfg.setDefaultEncoding("UTF-8");
cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

ReportEngine engine = new ReportEngine(new FreemarkerTemplateEngine(cfg));
```

### ThymeleafTemplateEngine

Backed by [Thymeleaf](https://www.thymeleaf.org/). Templates are loaded from `/templates/` on the classpath by default (`.html` files).

> **Important:** Pass the template name **without** the `.html` suffix — the resolver appends it automatically.

```java
// Default configuration — loads from /templates/ on the classpath
ReportEngine engine = new ReportEngine(new ThymeleafTemplateEngine());
engine.generateReport("invoice", data, outputFile); // resolves to /templates/invoice.html

// Custom Thymeleaf engine
ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
resolver.setPrefix("/my-templates/");
resolver.setSuffix(".html");
resolver.setTemplateMode(TemplateMode.HTML);

org.thymeleaf.TemplateEngine thymeleaf = new org.thymeleaf.TemplateEngine();
thymeleaf.setTemplateResolver(resolver);

ReportEngine engine = new ReportEngine(new ThymeleafTemplateEngine(thymeleaf));
```

### Custom TemplateEngine

Implement the `TemplateEngine` interface to plug in any other template technology:

```java
public class MustacheTemplateEngine implements TemplateEngine {
    @Override
    public String processTemplate(String templateName, Map<String, Object> data)
            throws ReportGenerationException {
        // ... your implementation
    }
}

ReportEngine engine = new ReportEngine(new MustacheTemplateEngine());
```

## Output Renderers

The `OutputRenderer` interface is the strategy for converting rendered HTML into the desired document format. You can use the built-in implementations or provide your own.

### PdfOutputRenderer (default)

Converts HTML to PDF using [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) (iText-based). This is the default renderer used when constructing a `ReportEngine` without specifying one.

```java
// Explicit (same as the no-arg constructor)
ReportEngine engine = new ReportEngine(new FreemarkerTemplateEngine(), new PdfOutputRenderer());
```

### DocxOutputRenderer

Converts HTML to DOCX using [Apache POI](https://poi.apache.org/) and [Jsoup](https://jsoup.org/) for HTML parsing.

Supported HTML elements:
- `<h1>` – `<h6>`: headings (bold, decreasing font size)
- `<p>`: paragraphs (with inline `<b>`, `<i>`, `<u>` support)
- `<ul>` / `<ol>`: unordered and ordered lists
- `<table>`: tables with `<thead>` / `<tbody>` / `<tr>` / `<th>` / `<td>`
- `<br>`: line breaks inside paragraphs

```java
// DOCX output with FreeMarker
ReportEngine docxEngine = new ReportEngine(new FreemarkerTemplateEngine(), new DocxOutputRenderer());
docxEngine.generateReport("sample-report.ftl", data, new File("report.docx"));

// DOCX output with Thymeleaf
ReportEngine thymeleafDocx = new ReportEngine(new ThymeleafTemplateEngine(), new DocxOutputRenderer());
thymeleafDocx.generateReport("sample-report", data, new File("report.docx"));
```

### Custom OutputRenderer

Implement `OutputRenderer` to support any other format:

```java
public class MarkdownRenderer implements OutputRenderer {
    @Override
    public byte[] render(String html) throws ReportGenerationException {
        // convert HTML to Markdown bytes
    }
}

ReportEngine engine = new ReportEngine(new FreemarkerTemplateEngine(), new MarkdownRenderer());
```

## Page Orientation

The engine supports both portrait and landscape orientations.

### Setting Orientation

```java
// Using convenience methods
new ReportBuilder(engine)
    .withTemplate("template.ftl")
    .landscape()  // Landscape orientation
    .generateTo(outputFile);

// Or using explicit PageOrientation
new ReportBuilder(engine)
    .withTemplate("template.ftl")
    .withOrientation(PageOrientation.PORTRAIT)  // Portrait orientation
    .generateTo(outputFile);
```

### Page Sizes

Supported page sizes:
- `PageSize.A4` - 210mm × 297mm (default)
- `PageSize.A3` - 297mm × 420mm
- `PageSize.A5` - 148mm × 210mm
- `PageSize.LETTER` - 8.5" × 11"
- `PageSize.LEGAL` - 8.5" × 14"

```java
new ReportBuilder(engine)
    .withTemplate("template.ftl")
    .landscape()
    .withPageSize(PageSize.LETTER)
    .generateTo(outputFile);
```

## Template Configuration

When using landscape orientation, the page orientation is automatically injected into the data model as `pageOrientation`, and the page size as `pageSize`.

### FreeMarker example

```html
<!DOCTYPE html>
<html>
<head>
    <style>
        @page {
            size: ${pageSize!"A4"} ${pageOrientation!"portrait"};
            margin: 2cm;
        }
    </style>
</head>
<body>
    <!-- Your content here -->
</body>
</html>
```

### Thymeleaf example

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <style th:inline="text">
        @page {
            size: [[${pageSize ?: 'A4'}]] [[${pageOrientation ?: 'portrait'}]];
            margin: 2cm;
        }
    </style>
</head>
<body>
    <!-- Your content here -->
</body>
</html>
```

## PageStyleHelper

`PageStyleHelper` is a utility class for generating CSS `@page` rules programmatically.

```java
// Generate a CSS @page rule: "@page { size: A4 landscape; margin: 2cm; }"
String style = PageStyleHelper.generatePageStyle("A4", "landscape");

// Custom margins
String style = PageStyleHelper.generatePageStyle("letter", "portrait", "1.5cm");

// Generate rules for both orientations (mixed-orientation documents)
String styles = PageStyleHelper.generateMixedOrientationStyles("A4");
// → "@page :portrait { size: A4 portrait; margin: 2cm; } @page :landscape { ... }"

// Custom margins for mixed orientation
String styles = PageStyleHelper.generateMixedOrientationStyles("A4", "1.5cm");
```

## Examples

### 1. Sample Report Generation

```java
ReportEngine engine = new ReportEngine();

Map<String, Object> data = new HashMap<>();
data.put("title", "Monthly Sales Report");
data.put("subtitle", "Q4 2025");
data.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

// Add items
List<Map<String, Object>> items = new ArrayList<>();
items.add(Map.of(
    "id", "001",
    "name", "Widget A",
    "description", "High-quality widget",
    "quantity", 10,
    "price", 25.99
));
data.put("items", items);
data.put("total", 259.90);

// Generate
new ReportBuilder(engine)
    .withTemplate("sample-report.ftl")
    .withData(data)
    .generateTo(new File("sales-report.pdf"));
```

### 2. Sample Report with Thymeleaf

```java
// Switch to Thymeleaf — use the sample-report.html template (no .html suffix)
ReportEngine engine = new ReportEngine(new ThymeleafTemplateEngine());

Map<String, Object> data = new HashMap<>();
data.put("title", "Monthly Sales Report");
data.put("subtitle", "Q4 2025");
data.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
data.put("author", "Sales Department");

List<Map<String, Object>> items = new ArrayList<>();
items.add(Map.of(
    "id", "001",
    "name", "Widget A",
    "description", "High-quality widget",
    "quantity", 10,
    "price", 25.99
));
data.put("items", items);
data.put("total", 259.90);

// Template resolves to /templates/sample-report.html on the classpath
new ReportBuilder(engine)
    .withTemplate("sample-report")
    .withData(data)
    .generateTo(new File("sales-report.pdf"));
```

### 3. Landscape Report Generation

```java
ReportEngine engine = new ReportEngine();

Map<String, Object> data = new HashMap<>();
data.put("title", "Wide Format Report");

List<Map<String, Object>> items = new ArrayList<>();
// ... add items ...
data.put("items", items);

// Generate in landscape orientation
new ReportBuilder(engine)
    .withTemplate("landscape-report.ftl")
    .landscape()             // Wide format for detailed data
    .withPageSize(PageSize.A4)
    .withData(data)
    .generateTo(new File("landscape-report.pdf"));
```

Landscape orientation is ideal for:
- Financial reports with many columns
- Inventory or product catalogs
- Statistical data with wide tables
- Comparative analysis documents
- Technical specifications

### 4. Invoice Generation

```java
ReportEngine engine = new ReportEngine();

Map<String, Object> data = new HashMap<>();

// Company info
data.put("companyName", "Acme Corporation");
data.put("companyAddress", "123 Business Street");
data.put("companyCity", "New York");
data.put("companyState", "NY");
data.put("companyZip", "10001");
data.put("companyPhone", "(555) 123-4567");

// Invoice info
data.put("invoiceNumber", "INV-2026-0001");
data.put("invoiceDate", "02/26/2026");
data.put("dueDate", "03/28/2026");

// Customer info
data.put("customerName", "Client Company");
data.put("customerAddress", "456 Client Ave");
// ... more customer fields

// Line items
List<Map<String, Object>> lineItems = new ArrayList<>();
lineItems.add(Map.of(
    "description", "Professional Services",
    "quantity", 40,
    "unitPrice", 150.00
));
data.put("lineItems", lineItems);

// Totals
data.put("subtotal", 6000.00);
data.put("tax", 480.00);
data.put("total", 6480.00);

// Generate invoice
new ReportBuilder(engine)
    .withTemplate("invoice.ftl")
    .withData(data)
    .generateTo(new File("invoice.pdf"));
```

### 5. DOCX Generation with Thymeleaf

```java
// Thymeleaf + DOCX output
ReportEngine engine = new ReportEngine(new ThymeleafTemplateEngine(), new DocxOutputRenderer());

Map<String, Object> data = new HashMap<>();
data.put("title", "My Word Document");
data.put("generatedDate", LocalDate.now().toString());

// Template resolves to /templates/sample-report.html on the classpath
engine.generateReport("sample-report", data, new File("report.docx"));

// Alternatively, use the builder convenience method on any engine
ReportEngine pdfEngine = new ReportEngine(new ThymeleafTemplateEngine());
byte[] docxBytes = new ReportBuilder(pdfEngine)
    .withTemplate("sample-report")
    .withData(data)
    .generateAsDocx();  // overrides renderer to DocxOutputRenderer for this call only
```

## Creating Custom Templates

Templates are stored in `src/main/resources/templates/` and use either FreeMarker Template Language (`.ftl`) or Thymeleaf (`.html`).

### FreeMarker Template Structure

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>${title}</title>
    <style>
        @page {
            size: ${pageSize!"A4"} ${pageOrientation!"portrait"};
            margin: 2cm;
        }
        body {
            font-family: Arial, sans-serif;
        }
    </style>
</head>
<body>
    <h1>${title}</h1>

    <#if items?? && items?size gt 0>
        <table>
            <#list items as item>
                <tr>
                    <td>${item.name}</td>
                    <td>${item.price?string["0.00"]}</td>
                </tr>
            </#list>
        </table>
    </#if>
</body>
</html>
```

### FreeMarker Features Used

- **Variables**: `${variableName}`
- **Conditionals**: `<#if condition>...</#if>`
- **Loops**: `<#list items as item>...</#list>`
- **Number formatting**: `${price?string["0.00"]}`
- **Null checks**: `<#if variable??>...</#if>`
- **Default values**: `${description!"N/A"}`

### Thymeleaf Template Structure

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title th:text="${title}">Report</title>
    <style th:inline="text">
        @page {
            size: [[${pageSize ?: 'A4'}]] [[${pageOrientation ?: 'portrait'}]];
            margin: 2cm;
        }
        body { font-family: Arial, sans-serif; }
    </style>
</head>
<body>
    <h1 th:text="${title}"></h1>

    <table th:if="${items != null and !#lists.isEmpty(items)}">
        <tr th:each="item : ${items}">
            <td th:text="${item.name}"></td>
            <td th:text="${#numbers.formatDecimal(item.price, 1, 2)}"></td>
        </tr>
    </table>
</body>
</html>
```

### Thymeleaf Features Used

- **Text output**: `th:text="${variableName}"` or inline `[[${variableName}]]`
- **Conditionals**: `th:if="${condition}"`
- **Loops**: `th:each="item : ${items}"`
- **Number formatting**: `${#numbers.formatDecimal(price, 1, 2)}`
- **Null-safe defaults**: `${value ?: 'default'}`
- **Inline CSS/JS**: `th:inline="text"` with `[[${expr}]]` syntax
- **Namespace**: declare `xmlns:th="http://www.thymeleaf.org"` on the root element

## API Reference

### TemplateEngine

Strategy interface for template processing. Both built-in implementations and custom ones must implement this interface.

#### Method
- `String processTemplate(String templateName, Map<String, Object> data)` — Processes the template and returns rendered HTML. Throws `ReportGenerationException` on failure.

---

### FreemarkerTemplateEngine

`TemplateEngine` implementation backed by Apache FreeMarker.

#### Constructors
- `FreemarkerTemplateEngine()` — Default configuration; loads templates from `/templates` on the classpath
- `FreemarkerTemplateEngine(Configuration freemarkerConfig)` — Custom FreeMarker configuration

#### Methods
- `Configuration getFreemarkerConfig()` — Returns the underlying FreeMarker `Configuration`

---

### ThymeleafTemplateEngine

`TemplateEngine` implementation backed by Thymeleaf.

> Template names must be supplied **without** the `.html` suffix.

#### Constructors
- `ThymeleafTemplateEngine()` — Default configuration; loads templates from `/templates/` on the classpath with suffix `.html`
- `ThymeleafTemplateEngine(org.thymeleaf.TemplateEngine thymeleafEngine)` — Custom Thymeleaf engine

#### Methods
- `org.thymeleaf.TemplateEngine getThymeleafEngine()` — Returns the underlying Thymeleaf engine

---

### OutputRenderer

Strategy interface for converting rendered HTML into the target document format.

#### Method
- `byte[] render(String html)` — Converts HTML to the target format. Throws `ReportGenerationException` on failure.

---

### PdfOutputRenderer

`OutputRenderer` implementation that converts HTML to PDF using Flying Saucer. This is the **default** renderer.

#### Constructor
- `PdfOutputRenderer()` — No configuration needed

---

### DocxOutputRenderer

`OutputRenderer` implementation that converts HTML to DOCX using Apache POI and Jsoup.

#### Constructor
- `DocxOutputRenderer()` — No configuration needed

---

### ReportEngine

Main class for report generation.

#### Constructors
- `ReportEngine()` — Uses the default `FreemarkerTemplateEngine` and `PdfOutputRenderer`
- `ReportEngine(TemplateEngine templateEngine)` — Uses the provided `TemplateEngine` with `PdfOutputRenderer`
- `ReportEngine(TemplateEngine templateEngine, OutputRenderer outputRenderer)` — Fully custom template engine and output renderer
- `ReportEngine(freemarker.template.Configuration freemarkerConfig)` — Backward-compatible; wraps the config in a `FreemarkerTemplateEngine` with `PdfOutputRenderer`

#### Methods
- `generateReport(String templateName, Map<String, Object> data, OutputStream outputStream)` — Generates document to stream
- `generateReport(String templateName, Map<String, Object> data, File outputFile)` — Generates document to file
- `generateReportAsBytes(String templateName, Map<String, Object> data)` — Generates document as byte array
- `getTemplateEngine()` — Returns the configured `TemplateEngine`
- `getOutputRenderer()` — Returns the configured `OutputRenderer`

---

### ReportBuilder

Fluent API builder for creating reports.

#### Constructor
- `ReportBuilder(ReportEngine engine)` — Creates a builder using the given engine

#### Methods
- `withTemplate(String templateName)` — Sets the template
- `withData(String key, Object value)` — Adds a single data entry
- `withData(Map<String, Object> data)` — Adds multiple data entries
- `clearData()` — Clears all data
- `withOrientation(PageOrientation orientation)` — Sets the page orientation
- `landscape()` — Convenience method for landscape orientation
- `portrait()` — Convenience method for portrait orientation
- `withPageSize(PageSize pageSize)` — Sets the page size
- `withOutputRenderer(OutputRenderer renderer)` — Overrides the renderer for this single report (does not modify the engine)
- `generateTo(OutputStream)` — Generates to stream
- `generateTo(File)` — Generates to file
- `generateAsBytes()` — Generates as byte array using the engine's renderer
- `generateAsDocx()` — Convenience method: generates as DOCX byte array (equivalent to `.withOutputRenderer(new DocxOutputRenderer()).generateAsBytes()`)

---

### PageStyleHelper

Utility class for generating CSS `@page` rules.

#### Methods
- `generatePageStyle(String pageSize, String orientation)` — Returns a `@page` rule with default 2 cm margins
- `generatePageStyle(String pageSize, String orientation, String margins)` — Returns a `@page` rule with custom margins
- `generateMixedOrientationStyles(String pageSize)` — Returns `@page :portrait` and `@page :landscape` rules with default margins
- `generateMixedOrientationStyles(String pageSize, String margins)` — Same as above with custom margins

---

### PageOrientation

Enumeration for page orientations:
- `PageOrientation.PORTRAIT` — Portrait orientation (default)
- `PageOrientation.LANDSCAPE` — Landscape orientation

---

### PageSize

Enumeration for standard page sizes:
- `PageSize.A4` — A4 (210mm × 297mm) — default
- `PageSize.A3` — A3 (297mm × 420mm)
- `PageSize.A5` — A5 (148mm × 210mm)
- `PageSize.LETTER` — Letter (8.5" × 11")
- `PageSize.LEGAL` — Legal (8.5" × 14")

## Running Examples

Run the provided examples to see the engine in action:

```bash
# Sample Report Example
./gradlew run -PmainClass=dev.avelar.jambock.examples.SampleReportExample

# Landscape Report Example
./gradlew run -PmainClass=dev.avelar.jambock.examples.LandscapeReportExample

# Invoice Example
./gradlew run -PmainClass=dev.avelar.jambock.examples.InvoiceExample
```

## Running Tests

```bash
./gradlew test
```

## Advanced Configuration

### Custom FreeMarker Configuration

```java
Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
cfg.setClassForTemplateLoading(MyClass.class, "/my-templates");
cfg.setDefaultEncoding("UTF-8");
cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

ReportEngine engine = new ReportEngine(new FreemarkerTemplateEngine(cfg));
```

### Loading FreeMarker Templates from the File System

```java
Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
cfg.setDirectoryForTemplateLoading(new File("/path/to/templates"));

ReportEngine engine = new ReportEngine(new FreemarkerTemplateEngine(cfg));
```

### Custom Thymeleaf Configuration

```java
ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
resolver.setPrefix("/my-templates/");
resolver.setSuffix(".html");
resolver.setTemplateMode(TemplateMode.HTML);
resolver.setCharacterEncoding("UTF-8");
resolver.setCacheable(true);

org.thymeleaf.TemplateEngine thymeleaf = new org.thymeleaf.TemplateEngine();
thymeleaf.setTemplateResolver(resolver);

ReportEngine engine = new ReportEngine(new ThymeleafTemplateEngine(thymeleaf));
```

### Combining Thymeleaf with DOCX Output

```java
ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
resolver.setPrefix("/reports/");
resolver.setSuffix(".html");
resolver.setTemplateMode(TemplateMode.HTML);

org.thymeleaf.TemplateEngine thymeleaf = new org.thymeleaf.TemplateEngine();
thymeleaf.setTemplateResolver(resolver);

ReportEngine engine = new ReportEngine(
    new ThymeleafTemplateEngine(thymeleaf),
    new DocxOutputRenderer()
);
engine.generateReport("my-report", data, new File("my-report.docx"));
```

## CSS Styling Tips

Flying Saucer supports most CSS 2.1 features. Here are some tips:

- Use `@page` rule to set page size and margins (or use `PageStyleHelper`)
- Supported page sizes: A4, A3, A5, Letter, Legal
- Use `page-break-before` and `page-break-after` for page breaks
- Flexbox is not fully supported; use tables for layout
- Web fonts need to be embedded or available on the system

> **Note:** CSS styling is applied during the PDF rendering phase (Flying Saucer). DOCX output via `DocxOutputRenderer` performs structural HTML-to-Word conversion and does not process CSS rules.

## Error Handling

All report generation methods throw `ReportGenerationException` which wraps underlying exceptions:

```java
try {
    engine.generateReport("template.ftl", data, outputFile);
} catch (ReportGenerationException e) {
    Logger.getLogger(MyClass.class.getName()).log(Level.SEVERE, "Failed to generate report", e);
    // Handle error
}
```

## License

This project is licensed under the MIT License.
