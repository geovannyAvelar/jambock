package dev.avelar.jambock.reports;

/**
 * Enumeration for page orientation in PDF reports.
 */
public enum PageOrientation {
    /**
     * Portrait orientation (default) - height is greater than width
     */
    PORTRAIT("portrait"),

    /**
     * Landscape orientation - width is greater than height
     */
    LANDSCAPE("landscape");

    private final String value;

    PageOrientation(String value) {
        this.value = value;
    }

    /**
     * Gets the orientation value for use in templates.
     *
     * @return the orientation string
     */
    public String getValue() {
        return value;
    }
}

