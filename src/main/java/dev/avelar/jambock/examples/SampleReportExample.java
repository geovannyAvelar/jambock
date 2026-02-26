package dev.avelar.jambock.examples;

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
 * Example demonstrating how to use the Report Engine to generate a sample report.
 */
public class SampleReportExample {

    public static void main(String[] args) {
        try {
            // Create the report engine
            ReportEngine engine = new ReportEngine();

            // Prepare the data model
            Map<String, Object> data = new HashMap<>();
            data.put("title", "Monthly Sales Report");
            data.put("subtitle", "Q4 2025 Performance Overview");
            data.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            data.put("author", "Sales Department");
            data.put("description", "This report summarizes the sales performance for the last quarter.");

            // Add items to the report
            List<Map<String, Object>> items = new ArrayList<>();
            items.add(createItem("001", "Widget A", "High-quality widget", 10, 25.99));
            items.add(createItem("002", "Widget B", "Premium widget", 5, 49.99));
            items.add(createItem("003", "Widget C", "Standard widget", 15, 19.99));
            items.add(createItem("004", "Widget D", "Deluxe widget", 8, 35.50));
            items.add(createItem("005", "Widget E", "Basic widget", 20, 12.99));

            data.put("items", items);

            // Calculate total
            double total = items.stream()
                    .mapToDouble(item -> (Integer) item.get("quantity") * (Double) item.get("price"))
                    .sum();
            data.put("total", total);

            // Add additional sections
            List<Map<String, Object>> sections = new ArrayList<>();
            sections.add(createSection("Executive Summary",
                    "This quarter has shown strong growth across all product lines. " +
                    "Widget sales increased by 25% compared to the previous quarter, " +
                    "driven primarily by Widget E's popularity in the consumer market."));
            sections.add(createSection("Key Insights",
                    "The data shows consistent demand for both premium and budget options. " +
                    "We recommend maintaining diverse product offerings to capture different market segments."));

            data.put("sections", sections);

            // Generate the report using the builder pattern
            File outputFile = new File("sample-report.pdf");
            new ReportBuilder(engine)
                    .withTemplate("sample-report.ftl")
                    .withData(data)
                    .generateTo(outputFile);

            System.out.println("Report generated successfully: " + outputFile.getAbsolutePath());

            // Alternative: Generate as bytes
            byte[] pdfBytes = new ReportBuilder(engine)
                    .withTemplate("sample-report.ftl")
                    .withData(data)
                    .generateAsBytes();

            System.out.println("Report generated as bytes: " + pdfBytes.length + " bytes");

        } catch (ReportGenerationException e) {
            System.err.println("Failed to generate report: " + e.getMessage());
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

