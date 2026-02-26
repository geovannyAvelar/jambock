# Jambock Reports Engine

A powerful PDF report generation engine using **Freemarker** templates and **Flying Saucer** for HTML-to-PDF conversion.

## Requirements

- **Java 8+** - Compatible with Java 8 and later versions

## Features

- ✅ Template-based report generation using Freemarker
- ✅ HTML to PDF conversion with Flying Saucer (based on iText)
- ✅ **Portrait and Landscape page orientations**
- ✅ **Multiple page sizes** (A4, Letter, Legal, A3, A5)
- ✅ Fluent API with builder pattern
- ✅ Support for CSS styling in templates
- ✅ Multiple output options (File, OutputStream, byte array)
- ✅ Pre-built templates (sample report and invoice)
- ✅ Comprehensive error handling
- ✅ Fully tested

## Dependencies

All dependencies are compatible with Java 8:

```kotlin
// Freemarker template engine
implementation("org.freemarker:freemarker:2.3.32")

// Flying Saucer for HTML to PDF conversion
implementation("org.xhtmlrenderer:flying-saucer-pdf:9.5.1")

// SLF4J for logging (Java 8 compatible version)
implementation("org.slf4j:slf4j-api:1.7.36")
implementation("org.slf4j:slf4j-simple:1.7.36")
```

## Quick Start

### Basic Usage

```java
// Create the report engine
ReportEngine engine = new ReportEngine();

// Prepare your data
Map<String, Object> data = new HashMap<>();
data.put("title", "Monthly Sales Report");
data.put("generatedDate", LocalDate.now().toString());

// Generate the report
File outputFile = new File("report.pdf");
engine.generateReport("sample-report.ftl", data, outputFile);
```

### Using the Builder Pattern

```java
ReportEngine engine = new ReportEngine();

byte[] pdfBytes = new ReportBuilder(engine)
    .withTemplate("sample-report.ftl")
    .withData("title", "My Report")
    .withData("author", "John Doe")
    .landscape()  // Set to landscape orientation
    .withPageSize(PageSize.A4)
    .withData(dataMap)  // Add multiple entries
    .generateAsBytes();
```

## Page Orientation

The reports engine supports both portrait and landscape orientations:

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

When using landscape orientation in your templates, the page orientation is automatically injected into the data model as `pageOrientation`:

```html
<!DOCTYPE html>
<html>
<head>
    <style>
        @page {
            size: A4 ${pageOrientation!"portrait"};
            margin: 2cm;
        }
    </style>
</head>
<body>
    <!-- Your content here -->
</body>
</html>
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
    .landscape()  // Wide format for detailed data
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

## Creating Custom Templates

Templates are stored in `src/main/resources/templates/` and use the Freemarker Template Language (FTL).

### Template Structure

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>${title}</title>
    <style>
        @page {
            size: A4;
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

### Freemarker Features Used

- **Variables**: `${variableName}`
- **Conditionals**: `<#if condition>...</#if>`
- **Loops**: `<#list items as item>...</#list>`
- **Number formatting**: `${price?string["0.00"]}`
- **Null checks**: `<#if variable??>...</#if>`
- **Default values**: `${description!"N/A"}`

## API Reference

### ReportEngine

Main class for report generation.

#### Constructor
- `ReportEngine()` - Creates engine with default configuration
- `ReportEngine(Configuration freemarkerConfig)` - Creates engine with custom Freemarker configuration

#### Methods
- `generateReport(String templateName, Map<String, Object> data, OutputStream outputStream)` - Generates PDF to stream
- `generateReport(String templateName, Map<String, Object> data, File outputFile)` - Generates PDF to file
- `generateReportAsBytes(String templateName, Map<String, Object> data)` - Generates PDF as byte array

### ReportBuilder

Fluent API builder for creating reports.

#### Methods
- `withTemplate(String templateName)` - Sets the template
- `withData(String key, Object value)` - Adds a single data entry
- `withData(Map<String, Object> data)` - Adds multiple data entries
- `clearData()` - Clears all data
- `withOrientation(PageOrientation orientation)` - Sets the page orientation
- `landscape()` - Convenience method for landscape orientation
- `portrait()` - Convenience method for portrait orientation
- `withPageSize(PageSize pageSize)` - Sets the page size
- `generateTo(OutputStream)` - Generates to stream
- `generateTo(File)` - Generates to file
- `generateAsBytes()` - Generates as byte array

### PageOrientation

Enumeration for page orientations:
- `PageOrientation.PORTRAIT` - Portrait orientation (default)
- `PageOrientation.LANDSCAPE` - Landscape orientation

### PageSize

Enumeration for standard page sizes:
- `PageSize.A4` - A4 (210mm × 297mm) - default
- `PageSize.A3` - A3 (297mm × 420mm)
- `PageSize.A5` - A5 (148mm × 210mm)
- `PageSize.LETTER` - Letter (8.5" × 11")
- `PageSize.LEGAL` - Legal (8.5" × 14")

## Running Examples

Run the provided examples to see the engine in action:

```bash
# Sample Report Example
./gradlew run -PmainClass=dev.avelar.jambock.examples.SampleReportExample

# Invoice Example
./gradlew run -PmainClass=dev.avelar.jambock.examples.InvoiceExample
```

## Running Tests

```bash
./gradlew test
```

## Advanced Configuration

### Custom Freemarker Configuration

```java
Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
cfg.setClassForTemplateLoading(MyClass.class, "/my-templates");
cfg.setDefaultEncoding("UTF-8");
cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

ReportEngine engine = new ReportEngine(cfg);
```

### Loading Templates from File System

```java
Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
cfg.setDirectoryForTemplateLoading(new File("/path/to/templates"));

ReportEngine engine = new ReportEngine(cfg);
```

## CSS Styling Tips

Flying Saucer supports most CSS 2.1 features. Here are some tips:

- Use `@page` rule to set page size and margins
- Supported page sizes: A4, Letter, etc.
- Use `page-break-before` and `page-break-after` for page breaks
- Flexbox is not fully supported; use tables for layout
- Web fonts need to be embedded or available on the system

## Error Handling

All report generation methods throw `ReportGenerationException` which wraps underlying exceptions:

```java
try {
    engine.generateReport("template.ftl", data, outputFile);
} catch (ReportGenerationException e) {
    logger.error("Failed to generate report", e);
    // Handle error
}
```

## License




