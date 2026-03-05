package dev.avelar.jambock.reports;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ReportEngine class (FreeMarker and Thymeleaf engines).
 */
class ReportEngineTest {

  @TempDir
  Path tempDir;

  private ReportEngine engine;
  private ReportEngine thymeleafEngine;

  @BeforeEach
  void setUp() {
    engine = new ReportEngine();                                      // FreeMarker (default)
    thymeleafEngine = new ReportEngine(new ThymeleafTemplateEngine()); // Thymeleaf
  }

  @Test
  void testGenerateReportToFile() throws ReportGenerationException {
    // Prepare test data
    Map<String, Object> data = createSampleReportData();

    // Generate report
    File outputFile = tempDir.resolve("test-report.pdf").toFile();
    engine.generateReport("sample-report.ftl", data, outputFile);

    // Verify file was created and has content
    assertTrue(outputFile.exists(), "Report file should exist");
    assertTrue(outputFile.length() > 0, "Report file should not be empty");
  }

  @Test
  void testGenerateReportAsBytes() throws ReportGenerationException {
    // Prepare test data
    Map<String, Object> data = createSampleReportData();

    // Generate report
    byte[] pdfBytes = engine.generateReportAsBytes("sample-report.ftl", data);

    // Verify bytes were generated
    assertNotNull(pdfBytes, "PDF bytes should not be null");
    assertTrue(pdfBytes.length > 0, "PDF bytes should not be empty");

    // Verify it's a PDF (check PDF header)
    String header = new String(Arrays.copyOfRange(pdfBytes, 0, 4));
    assertEquals("%PDF", header, "Should be a valid PDF file");
  }

  @Test
  void testReportBuilderWithTemplate() throws ReportGenerationException {
    Map<String, Object> data = createSampleReportData();

    byte[] pdfBytes = new ReportBuilder(engine).withTemplate("sample-report.ftl").withData(data).generateAsBytes();

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);
  }

  @Test
  void testReportBuilderWithIndividualDataEntries() throws ReportGenerationException {
    ReportBuilder builder = new ReportBuilder(engine).withTemplate("sample-report.ftl").withData("title", "Test Report")
        .withData("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")))
        .withData("description", "Test description");

    byte[] pdfBytes = builder.generateAsBytes();

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);
  }

  @Test
  void testReportBuilderThrowsExceptionWithoutTemplate() {
    ReportBuilder builder = new ReportBuilder(engine).withData("title", "Test");

    assertThrows(IllegalStateException.class, builder::generateAsBytes,
        "Should throw exception when template is not set");
  }

  @Test
  void testGenerateInvoice() throws ReportGenerationException {
    Map<String, Object> data = createInvoiceData();

    File outputFile = tempDir.resolve("test-invoice.pdf").toFile();
    engine.generateReport("invoice.ftl", data, outputFile);

    assertTrue(outputFile.exists(), "Invoice file should exist");
    assertTrue(outputFile.length() > 0, "Invoice file should not be empty");
  }

  @Test
  void testInvalidTemplateName() {
    Map<String, Object> data = createSampleReportData();

    assertThrows(ReportGenerationException.class, () -> {
      engine.generateReport("non-existent-template.ftl", data, tempDir.resolve("test.pdf").toFile());
    }, "Should throw exception for non-existent template");
  }

  @Test
  void testLandscapeOrientation() throws ReportGenerationException {
    Map<String, Object> data = createSampleReportData();

    File outputFile = tempDir.resolve("test-landscape.pdf").toFile();
    new ReportBuilder(engine).withTemplate("landscape-report.ftl").landscape().withData(data).generateTo(outputFile);

    assertTrue(outputFile.exists(), "Landscape report file should exist");
    assertTrue(outputFile.length() > 0, "Landscape report file should not be empty");
  }

  @Test
  void testPortraitOrientation() throws ReportGenerationException {
    Map<String, Object> data = createSampleReportData();

    File outputFile = tempDir.resolve("test-portrait.pdf").toFile();
    new ReportBuilder(engine).withTemplate("landscape-report.ftl").portrait().withData(data).generateTo(outputFile);

    assertTrue(outputFile.exists(), "Portrait report file should exist");
    assertTrue(outputFile.length() > 0, "Portrait report file should not be empty");
  }

  @Test
  void testPageSizeWithOrientation() throws ReportGenerationException {
    Map<String, Object> data = createSampleReportData();

    File outputFile = tempDir.resolve("test-letter-landscape.pdf").toFile();
    new ReportBuilder(engine).withTemplate("landscape-report.ftl").landscape().withPageSize(PageSize.LETTER)
        .withData(data).generateTo(outputFile);

    assertTrue(outputFile.exists(), "Letter-size landscape report should exist");
    assertTrue(outputFile.length() > 0, "Letter-size landscape report should not be empty");
  }

  @Test
  void testDifferentPageSizes() throws ReportGenerationException {
    Map<String, Object> data = createSampleReportData();

    for (PageSize size : PageSize.values()) {
      File outputFile = tempDir.resolve("test-" + size.getValue() + ".pdf").toFile();
      new ReportBuilder(engine).withTemplate("sample-report.ftl").withPageSize(size).withData(data)
          .generateTo(outputFile);

      assertTrue(outputFile.exists(), "Report with page size " + size + " should exist");
    }
  }

  @Test
  void testOrientationInDataModel() throws ReportGenerationException {
    Map<String, Object> data = new HashMap<>();
    data.put("title", "Test Report");
    data.put("generatedDate", "2026-02-26");
    data.put("pageOrientation", "landscape");
    data.put("pageSize", "A4");

    byte[] pdfBytes =
        new ReportBuilder(engine)
            .withTemplate("landscape-report.ftl")
            .landscape()
            .withPageSize(PageSize.A4)
            .withData(data)
            .generateAsBytes();

    // Verify PDF was generated
    assertNotNull(pdfBytes, "PDF bytes should not be null");
    assertTrue(pdfBytes.length > 0, "PDF bytes should not be empty");

    // Verify that orientation and page size are added to data
    assertTrue(data.containsKey("pageOrientation"), "pageOrientation should be in data model");
    assertEquals("landscape", data.get("pageOrientation"), "pageOrientation should be 'landscape'");
    assertTrue(data.containsKey("pageSize"), "pageSize should be in data model");
    assertEquals("A4", data.get("pageSize"), "pageSize should be 'A4'");
  }

  private Map<String, Object> createSampleReportData() {
    Map<String, Object> data = new HashMap<>();
    data.put("title", "Test Report");
    data.put("subtitle", "Test Subtitle");
    data.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
    data.put("author", "Test Author");
    data.put("description", "Test Description");

    List<Map<String, Object>> items = new ArrayList<>();
    items.add(createItem("001", "Item 1", "Description 1", 10, 25.99));
    items.add(createItem("002", "Item 2", "Description 2", 5, 49.99));

    data.put("items", items);
    data.put("total", 509.85);

    return data;
  }

  private Map<String, Object> createInvoiceData() {
    Map<String, Object> data = new HashMap<>();

    data.put("companyName", "Test Company");
    data.put("companyAddress", "123 Test St");
    data.put("companyCity", "Test City");
    data.put("companyState", "TS");
    data.put("companyZip", "12345");
    data.put("companyPhone", "(555) 555-5555");

    data.put("invoiceNumber", "TEST-001");
    data.put("invoiceDate", "02/26/2026");
    data.put("dueDate", "03/28/2026");

    data.put("customerName", "Test Customer");
    data.put("customerAddress", "456 Customer Ave");
    data.put("customerCity", "Customer City");
    data.put("customerState", "CS");
    data.put("customerZip", "67890");

    List<Map<String, Object>> lineItems = new ArrayList<>();
    lineItems.add(createLineItem("Test Service", 10, 100.00));

    data.put("lineItems", lineItems);
    data.put("subtotal", 1000.00);
    data.put("tax", 80.00);
    data.put("taxRate", "8.00");
    data.put("total", 1080.00);

    return data;
  }

  private Map<String, Object> createItem(String id, String name, String description, int quantity, double price) {
    Map<String, Object> item = new HashMap<>();
    item.put("id", id);
    item.put("name", name);
    item.put("description", description);
    item.put("quantity", quantity);
    item.put("price", price);
    return item;
  }

  private Map<String, Object> createLineItem(String description, int quantity, double unitPrice) {
    Map<String, Object> item = new HashMap<>();
    item.put("description", description);
    item.put("quantity", quantity);
    item.put("unitPrice", unitPrice);
    return item;
  }

  // =========================================================================
  // Polymorphic engine tests
  // =========================================================================

  @Test
  void testDefaultEngineIsFreemarker() {
    assertInstanceOf(FreemarkerTemplateEngine.class, engine.getTemplateEngine(),
        "Default engine should be FreemarkerTemplateEngine");
  }

  @Test
  void testThymeleafEngineIsSet() {
    assertInstanceOf(ThymeleafTemplateEngine.class, thymeleafEngine.getTemplateEngine(),
        "Engine should be ThymeleafTemplateEngine");
  }

  // =========================================================================
  // Thymeleaf engine tests
  // =========================================================================

  @Test
  void testThymeleafGenerateReportToFile() throws ReportGenerationException {
    Map<String, Object> data = createSampleReportData();

    File outputFile = tempDir.resolve("thymeleaf-report.pdf").toFile();
    thymeleafEngine.generateReport("sample-report", data, outputFile);

    assertTrue(outputFile.exists(), "Thymeleaf report file should exist");
    assertTrue(outputFile.length() > 0, "Thymeleaf report file should not be empty");
  }

  @Test
  void testThymeleafGenerateReportAsBytes() throws ReportGenerationException {
    Map<String, Object> data = createSampleReportData();

    byte[] pdfBytes = thymeleafEngine.generateReportAsBytes("sample-report", data);

    assertNotNull(pdfBytes, "Thymeleaf PDF bytes should not be null");
    assertTrue(pdfBytes.length > 0, "Thymeleaf PDF bytes should not be empty");

    String header = new String(Arrays.copyOfRange(pdfBytes, 0, 4));
    assertEquals("%PDF", header, "Should be a valid PDF file");
  }

  @Test
  void testThymeleafReportBuilderWithTemplate() throws ReportGenerationException {
    Map<String, Object> data = createSampleReportData();

    byte[] pdfBytes = new ReportBuilder(thymeleafEngine)
        .withTemplate("sample-report")
        .withData(data)
        .generateAsBytes();

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);
  }

  @Test
  void testThymeleafInvalidTemplateName() {
    Map<String, Object> data = createSampleReportData();

    assertThrows(ReportGenerationException.class, () ->
        thymeleafEngine.generateReport("non-existent-template", data, tempDir.resolve("test.pdf").toFile()),
        "Should throw exception for non-existent Thymeleaf template");
  }

  // =========================================================================
  // DOCX output renderer tests
  // =========================================================================

  @Test
  void testDocxOutputRendererDirectly() throws ReportGenerationException {
    String html = "<html><body><h1>Test Title</h1><p>Hello World</p></body></html>";
    DocxOutputRenderer renderer = new DocxOutputRenderer();
    byte[] docxBytes = renderer.render(html);

    assertNotNull(docxBytes, "DOCX bytes should not be null");
    assertTrue(docxBytes.length > 0, "DOCX bytes should not be empty");

    // DOCX files are ZIP-based: check PK header
    assertEquals(0x50, docxBytes[0] & 0xFF, "Should start with PK (ZIP) header byte 0");
    assertEquals(0x4B, docxBytes[1] & 0xFF, "Should start with PK (ZIP) header byte 1");
  }

  @Test
  void testDocxOutputRendererWithTable() throws ReportGenerationException {
    String html = "<html><body>" +
        "<table><thead><tr><th>Name</th><th>Price</th></tr></thead>" +
        "<tbody><tr><td>Widget A</td><td>25.99</td></tr></tbody></table>" +
        "</body></html>";
    DocxOutputRenderer renderer = new DocxOutputRenderer();
    byte[] docxBytes = renderer.render(html);

    assertNotNull(docxBytes);
    assertTrue(docxBytes.length > 0);
  }

  @Test
  void testDocxOutputRendererWithList() throws ReportGenerationException {
    String html = "<html><body><ul><li>Item 1</li><li>Item 2</li></ul></body></html>";
    DocxOutputRenderer renderer = new DocxOutputRenderer();
    byte[] docxBytes = renderer.render(html);

    assertNotNull(docxBytes);
    assertTrue(docxBytes.length > 0);
  }

  @Test
  void testReportEngineWithDocxRenderer() throws ReportGenerationException {
    ReportEngine docxEngine = new ReportEngine(new FreemarkerTemplateEngine(), new DocxOutputRenderer());
    Map<String, Object> data = createSampleReportData();

    byte[] docxBytes = docxEngine.generateReportAsBytes("sample-report.ftl", data);

    assertNotNull(docxBytes, "DOCX bytes should not be null");
    assertTrue(docxBytes.length > 0, "DOCX bytes should not be empty");
    assertEquals(0x50, docxBytes[0] & 0xFF, "Should be a valid DOCX (ZIP) file");
  }

  @Test
  void testReportEngineWithDocxRendererToFile() throws ReportGenerationException {
    ReportEngine docxEngine = new ReportEngine(new FreemarkerTemplateEngine(), new DocxOutputRenderer());
    Map<String, Object> data = createSampleReportData();

    File outputFile = tempDir.resolve("test-report.docx").toFile();
    docxEngine.generateReport("sample-report.ftl", data, outputFile);

    assertTrue(outputFile.exists(), "DOCX file should exist");
    assertTrue(outputFile.length() > 0, "DOCX file should not be empty");
  }

  @Test
  void testReportBuilderGenerateAsDocx() throws ReportGenerationException {
    Map<String, Object> data = createSampleReportData();

    byte[] docxBytes = new ReportBuilder(engine)
        .withTemplate("sample-report.ftl")
        .withData(data)
        .generateAsDocx();

    assertNotNull(docxBytes, "DOCX bytes should not be null");
    assertTrue(docxBytes.length > 0, "DOCX bytes should not be empty");
    assertEquals(0x50, docxBytes[0] & 0xFF, "Should be a valid DOCX (ZIP) file");
  }

  @Test
  void testReportBuilderWithOutputRendererOverride() throws ReportGenerationException {
    Map<String, Object> data = createSampleReportData();

    byte[] docxBytes = new ReportBuilder(engine)
        .withTemplate("sample-report.ftl")
        .withData(data)
        .withOutputRenderer(new DocxOutputRenderer())
        .generateAsBytes();

    assertNotNull(docxBytes);
    assertTrue(docxBytes.length > 0);
    assertEquals(0x50, docxBytes[0] & 0xFF, "Should be a valid DOCX (ZIP) file");
  }

  @Test
  void testDocxRendererIsSetOnEngine() {
    ReportEngine docxEngine = new ReportEngine(new FreemarkerTemplateEngine(), new DocxOutputRenderer());
    assertInstanceOf(DocxOutputRenderer.class, docxEngine.getOutputRenderer(),
        "Output renderer should be DocxOutputRenderer");
  }

  @Test
  void testDefaultRendererIsPdf() {
    assertInstanceOf(PdfOutputRenderer.class, engine.getOutputRenderer(),
        "Default output renderer should be PdfOutputRenderer");
  }
}

