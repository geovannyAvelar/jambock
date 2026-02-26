package dev.avelar.jambock.reports;

/**
 * Enumeration for standard page sizes in PDF reports.
 */
public enum PageSize {
    /**
     * A4 size: 210mm × 297mm
     */
    A4("A4"),

    /**
     * Letter size: 8.5" × 11"
     */
    LETTER("letter"),

    /**
     * Legal size: 8.5" × 14"
     */
    LEGAL("legal"),

    /**
     * A3 size: 297mm × 420mm
     */
    A3("A3"),

    /**
     * A5 size: 148mm × 210mm
     */
    A5("A5");

    private final String value;

    PageSize(String value) {
        this.value = value;
    }

    /**
     * Gets the page size value for use in templates.
     *
     * @return the page size string
     */
    public String getValue() {
        return value;
    }
}

