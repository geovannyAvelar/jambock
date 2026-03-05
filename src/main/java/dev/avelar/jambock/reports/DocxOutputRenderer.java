package dev.avelar.jambock.reports;

import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * {@link OutputRenderer} implementation that converts HTML to a DOCX document using
 * <a href="https://poi.apache.org/">Apache POI</a> and
 * <a href="https://jsoup.org/">Jsoup</a> for HTML parsing.
 *
 * <p>Supported HTML elements:
 * <ul>
 *   <li>{@code <h1>} – {@code <h6>}: headings (bold, decreasing font size)</li>
 *   <li>{@code <p>}: paragraphs (with inline bold/italic/underline handling)</li>
 *   <li>{@code <ul>} / {@code <ol>}: unordered and ordered lists</li>
 *   <li>{@code <table>}: tables with {@code <thead>} / {@code <tbody>} / {@code <tr>} /
 *       {@code <th>} / {@code <td>} support</li>
 *   <li>{@code <br>}: line breaks inside paragraphs</li>
 * </ul>
 */
public class DocxOutputRenderer implements OutputRenderer {

    private static final int DEFAULT_FONT_SIZE_PT = 11;

    /**
     * Converts the supplied HTML string into a DOCX document.
     *
     * @param html the fully-rendered HTML string produced by a {@link TemplateEngine}
     * @return the DOCX content as a byte array
     * @throws ReportGenerationException if document creation or serialisation fails
     */
    @Override
    public byte[] render(String html) throws ReportGenerationException {
        try (XWPFDocument document = new XWPFDocument()) {
            Document jsoupDoc = Jsoup.parse(html);
            processBody(document, jsoupDoc.body());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate DOCX document: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Body traversal
    // -------------------------------------------------------------------------

    private void processBody(XWPFDocument document, Element body) {
        for (Element element : body.children()) {
            processElement(document, element);
        }
    }

    private void processElement(XWPFDocument document, Element element) {
        String tag = element.tagName().toLowerCase();
        switch (tag) {
            case "h1": case "h2": case "h3":
            case "h4": case "h5": case "h6":
                processHeading(document, element, tag);
                break;
            case "p":
                processParagraph(document, element);
                break;
            case "ul":
                processList(document, element, false);
                break;
            case "ol":
                processList(document, element, true);
                break;
            case "table":
                processTable(document, element);
                break;
            case "div": case "section": case "article": case "main":
            case "header": case "footer":
                // Recurse into block container elements
                for (Element child : element.children()) {
                    processElement(document, child);
                }
                break;
            default:
                String text = element.text().trim();
                if (!text.isEmpty()) {
                    XWPFParagraph para = document.createParagraph();
                    XWPFRun run = para.createRun();
                    run.setText(text);
                    run.setFontSize(DEFAULT_FONT_SIZE_PT);
                }
        }
    }

    // -------------------------------------------------------------------------
    // Headings
    // -------------------------------------------------------------------------

    private void processHeading(XWPFDocument document, Element element, String tag) {
        XWPFParagraph para = document.createParagraph();
        para.setSpacingAfter(120);

        XWPFRun run = para.createRun();
        run.setBold(true);
        run.setText(element.text());

        switch (tag) {
            case "h1": run.setFontSize(24); break;
            case "h2": run.setFontSize(20); break;
            case "h3": run.setFontSize(16); break;
            case "h4": run.setFontSize(14); break;
            case "h5": run.setFontSize(12); break;
            default:   run.setFontSize(11); break;
        }
    }

    // -------------------------------------------------------------------------
    // Paragraphs (with inline formatting)
    // -------------------------------------------------------------------------

    private void processParagraph(XWPFDocument document, Element element) {
        XWPFParagraph para = document.createParagraph();
        para.setSpacingAfter(80);
        applyInlineContent(para, element);
    }

    /**
     * Walks the child nodes of {@code element} and creates styled {@link XWPFRun}s for each
     * text node and inline element ({@code <b>}, {@code <strong>}, {@code <i>}, {@code <em>},
     * {@code <u>}, {@code <br>}, {@code <span>}).
     */
    private void applyInlineContent(XWPFParagraph para, Element element) {
        for (org.jsoup.nodes.Node node : element.childNodes()) {
            if (node instanceof org.jsoup.nodes.TextNode) {
                String text = ((org.jsoup.nodes.TextNode) node).text();
                if (!text.isEmpty()) {
                    XWPFRun run = para.createRun();
                    run.setText(text);
                    run.setFontSize(DEFAULT_FONT_SIZE_PT);
                }
            } else if (node instanceof Element) {
                Element child = (Element) node;
                String childTag = child.tagName().toLowerCase();
                switch (childTag) {
                    case "b": case "strong": {
                        XWPFRun run = para.createRun();
                        run.setBold(true);
                        run.setText(child.text());
                        run.setFontSize(DEFAULT_FONT_SIZE_PT);
                        break;
                    }
                    case "i": case "em": {
                        XWPFRun run = para.createRun();
                        run.setItalic(true);
                        run.setText(child.text());
                        run.setFontSize(DEFAULT_FONT_SIZE_PT);
                        break;
                    }
                    case "u": {
                        XWPFRun run = para.createRun();
                        run.setUnderline(UnderlinePatterns.SINGLE);
                        run.setText(child.text());
                        run.setFontSize(DEFAULT_FONT_SIZE_PT);
                        break;
                    }
                    case "br": {
                        XWPFRun run = para.createRun();
                        run.addBreak();
                        break;
                    }
                    default: {
                        String text = child.text().trim();
                        if (!text.isEmpty()) {
                            XWPFRun run = para.createRun();
                            run.setText(text);
                            run.setFontSize(DEFAULT_FONT_SIZE_PT);
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Lists
    // -------------------------------------------------------------------------

    private void processList(XWPFDocument document, Element listElement, boolean ordered) {
        int index = 1;
        for (Element li : listElement.select("> li")) {
            XWPFParagraph para = document.createParagraph();
            para.setIndentationLeft(720); // 0.5 inch
            XWPFRun run = para.createRun();
            String prefix = ordered ? (index++) + ". " : "\u2022 ";
            run.setText(prefix + li.text());
            run.setFontSize(DEFAULT_FONT_SIZE_PT);
        }
    }

    // -------------------------------------------------------------------------
    // Tables
    // -------------------------------------------------------------------------

    private void processTable(XWPFDocument document, Element tableElement) {
        // Collect all rows from thead + tbody (or directly from table)
        Elements rows = tableElement.select("thead tr, tbody tr");
        if (rows.isEmpty()) {
            rows = tableElement.select("tr");
        }
        if (rows.isEmpty()) return;

        // Determine column count from the first row
        int colCount = rows.first().select("th, td").size();
        if (colCount == 0) return;

        XWPFTable table = document.createTable(rows.size(), colCount);
        table.setWidth("100%");
        // Remove default 1-row that POI creates; we'll fill row by row
        removeExtraRows(table, rows.size());

        for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
            Element row = rows.get(rowIdx);
            Elements cells = row.select("th, td");
            XWPFTableRow tableRow = table.getRow(rowIdx);

            for (int colIdx = 0; colIdx < cells.size(); colIdx++) {
                Element cell = cells.get(colIdx);
                boolean isHeader = "th".equals(cell.tagName().toLowerCase());

                XWPFTableCell tableCell = tableRow.getCell(colIdx);
                if (tableCell == null) {
                    tableCell = tableRow.addNewTableCell();
                }
                tableCell.removeParagraph(0);
                XWPFParagraph cellPara = tableCell.addParagraph();
                XWPFRun run = cellPara.createRun();
                run.setText(cell.text());
                run.setFontSize(DEFAULT_FONT_SIZE_PT);
                if (isHeader) {
                    run.setBold(true);
                }
            }
        }
    }

    /**
     * Apache POI pre-creates rows when calling {@code createTable(rows, cols)}.
     * This method removes any surplus rows so we start fresh.
     */
    private void removeExtraRows(XWPFTable table, int desiredRows) {
        int current = table.getNumberOfRows();
        for (int i = current - 1; i >= desiredRows; i--) {
            table.removeRow(i);
        }
        // If POI created fewer rows than needed, add the missing ones
        for (int i = current; i < desiredRows; i++) {
            table.createRow();
        }
    }
}


