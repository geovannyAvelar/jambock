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
 * Example demonstrating how to generate an invoice PDF.
 */
public class InvoiceExample {

    public static void main(String[] args) {
        try {
            // Create the report engine
            ReportEngine engine = new ReportEngine();

            // Prepare the invoice data
            Map<String, Object> data = new HashMap<>();

            // Company information
            data.put("companyName", "Acme Corporation");
            data.put("companyAddress", "123 Business Street");
            data.put("companyCity", "New York");
            data.put("companyState", "NY");
            data.put("companyZip", "10001");
            data.put("companyPhone", "(555) 123-4567");

            // Invoice information
            data.put("invoiceNumber", "INV-2026-0001");
            data.put("invoiceDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            data.put("dueDate", LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));

            // Customer information
            data.put("customerName", "Tech Solutions Inc.");
            data.put("customerAddress", "456 Client Avenue");
            data.put("customerCity", "Boston");
            data.put("customerState", "MA");
            data.put("customerZip", "02101");
            data.put("customerEmail", "billing@techsolutions.com");

            // Invoice details
            data.put("purchaseOrder", "PO-2026-5678");
            data.put("terms", "Net 30");
            data.put("status", "Due");

            // Line items
            List<Map<String, Object>> lineItems = new ArrayList<>();
            lineItems.add(createLineItem("Professional Services - January 2026", 40, 150.00));
            lineItems.add(createLineItem("Software License - Annual Subscription", 1, 2500.00));
            lineItems.add(createLineItem("Technical Support Package", 12, 200.00));
            lineItems.add(createLineItem("Custom Development Hours", 25, 175.00));

            data.put("lineItems", lineItems);

            // Calculate totals
            double subtotal = lineItems.stream()
                    .mapToDouble(item -> (Integer) item.get("quantity") * (Double) item.get("unitPrice"))
                    .sum();

            double discount = 500.00;
            double taxRate = 8.875;
            double tax = (subtotal - discount) * (taxRate / 100);
            double total = subtotal - discount + tax;

            data.put("subtotal", subtotal);
            data.put("discount", discount);
            data.put("taxRate", String.format("%.2f", taxRate));
            data.put("tax", tax);
            data.put("total", total);

            data.put("notes", "Payment can be made via check, wire transfer, or credit card. " +
                    "Please include invoice number on your payment.");

            // Generate the invoice
            File outputFile = new File("invoice.pdf");
            new ReportBuilder(engine)
                    .withTemplate("invoice.ftl")
                    .withData(data)
                    .generateTo(outputFile);

            System.out.println("Invoice generated successfully: " + outputFile.getAbsolutePath());
            System.out.println("Total amount: $" + String.format("%.2f", total));

        } catch (ReportGenerationException e) {
            System.err.println("Failed to generate invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Map<String, Object> createLineItem(String description, int quantity, double unitPrice) {
        Map<String, Object> item = new HashMap<>();
        item.put("description", description);
        item.put("quantity", quantity);
        item.put("unitPrice", unitPrice);
        return item;
    }
}

