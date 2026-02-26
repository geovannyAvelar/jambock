package dev.avelar.jambock.examples;

import dev.avelar.jambock.reports.PageOrientation;
import dev.avelar.jambock.reports.PageSize;
import dev.avelar.jambock.reports.ReportBuilder;
import dev.avelar.jambock.reports.ReportEngine;
import dev.avelar.jambock.reports.ReportGenerationException;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example demonstrating landscape orientation for PDF reports.
 */
public class LandscapeReportExample {

    public static void main(String[] args) {
        try {
            // Create the report engine
            ReportEngine engine = new ReportEngine();

            // Prepare the data model
            Map<String, Object> data = new HashMap<>();
            data.put("title", "Quarterly Sales Report - Landscape");
            data.put("subtitle", "Q1 2026 - Wide Format for Detailed Data");
            data.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            data.put("author", "Analytics Department");
            data.put("description", "Detailed quarterly sales data with landscape orientation for better readability.");

            // Add items to the report
            List<Map<String, Object>> items = new ArrayList<>();
            items.add(createItem("SKU-001", "Widget A", "Electronic component - High demand", 150, 125.50));
            items.add(createItem("SKU-002", "Widget B", "Electronic component - Standard", 200, 89.99));
            items.add(createItem("SKU-003", "Widget C", "Electronic component - Budget", 350, 45.00));
            items.add(createItem("SKU-004", "Widget D", "Electronic component - Premium", 75, 299.99));
            items.add(createItem("SKU-005", "Widget E", "Electronic component - Specialty", 120, 199.50));
            items.add(createItem("SKU-006", "Widget F", "Electronic component - Professional", 90, 449.99));

            data.put("items", items);

            // Calculate total
            double total = items.stream()
                    .mapToDouble(item -> (Integer) item.get("quantity") * (Double) item.get("price"))
                    .sum();
            data.put("total", total);

            // Add additional sections
            List<Map<String, Object>> sections = new ArrayList<>();
            sections.add(createSection("Market Analysis",
                    "The landscape orientation provides ample space for detailed product information and pricing. " +
                    "This format is ideal for reports with many columns or wide tables."));
            sections.add(createSection("Performance Insights",
                    "Wide format enables side-by-side comparison of multiple metrics, making trends and patterns " +
                    "more apparent. Perfect for financial reports, inventory management, and statistical data."));

            data.put("sections", sections);

            // Generate the report with LANDSCAPE orientation
            File outputFile = new File("landscape-report.pdf");
            new ReportBuilder(engine)
                    .withTemplate("landscape-report.ftl")
                    .landscape()  // Set to landscape orientation
                    .withPageSize(PageSize.A4)
                    .withData(data)
                    .generateTo(outputFile);

            System.out.println("✓ Landscape report generated successfully: " + outputFile.getAbsolutePath());
            System.out.println("  Orientation: LANDSCAPE");
            System.out.println("  Page Size: A4");
            System.out.println("  Total Amount: $" + String.format("%.2f", total));

            // Also generate a portrait version for comparison
            File portraitFile = new File("portrait-report.pdf");
            new ReportBuilder(engine)
                    .withTemplate("landscape-report.ftl")
                    .portrait()  // Set to portrait orientation
                    .withPageSize(PageSize.A4)
                    .withData(data)
                    .generateTo(portraitFile);

            System.out.println("\n✓ Portrait report generated successfully: " + portraitFile.getAbsolutePath());
            System.out.println("  Orientation: PORTRAIT");
            System.out.println("  Page Size: A4");

            // Generate with explicit PageOrientation enum
            File landscapeLetterFile = new File("landscape-letter-report.pdf");
            new ReportBuilder(engine)
                    .withTemplate("landscape-report.ftl")
                    .withOrientation(PageOrientation.LANDSCAPE)
                    .withPageSize(PageSize.LETTER)
                    .withData(data)
                    .generateTo(landscapeLetterFile);

            System.out.println("\n✓ Landscape Letter-size report generated: " + landscapeLetterFile.getAbsolutePath());
            System.out.println("  Orientation: LANDSCAPE");
            System.out.println("  Page Size: LETTER");

        } catch (ReportGenerationException e) {
            System.err.println("✗ Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Map<String, Object> createItem(String id, String name, String description,
                                                    int quantity, double price) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("description", description);
        item.put("quantity", quantity);
        item.put("price", price);
        return item;
    }

    private static Map<String, Object> createSection(String title, String content) {
        Map<String, Object> section = new HashMap<>();
        section.put("title", title);
        section.put("content", content);
        return section;
    }
}

