package dev.avelar.jambock.reports;

/**
 * Helper class for generating CSS @page rules with page orientation and size.
 * Used to simplify template creation with dynamic page settings.
 */
public class PageStyleHelper {

    /**
     * Generates a CSS @page rule with the specified orientation and size.
     *
     * @param pageSize the page size (e.g., "A4", "letter")
     * @param orientation the page orientation ("portrait" or "landscape")
     * @param margins the page margins (e.g., "2cm")
     * @return a CSS @page rule string
     */
    public static String generatePageStyle(String pageSize, String orientation, String margins) {
        return String.format(
                "@page { size: %s %s; margin: %s; }",
                pageSize, orientation, margins
        );
    }

    /**
     * Generates a CSS @page rule with default margins (2cm).
     *
     * @param pageSize the page size (e.g., "A4", "letter")
     * @param orientation the page orientation ("portrait" or "landscape")
     * @return a CSS @page rule string
     */
    public static String generatePageStyle(String pageSize, String orientation) {
        return generatePageStyle(pageSize, orientation, "2cm");
    }

    /**
     * Generates CSS @page rules for both portrait and landscape pages.
     * Useful for documents with mixed page orientations.
     *
     * @param pageSize the page size (e.g., "A4", "letter")
     * @param margins the page margins (e.g., "2cm")
     * @return a CSS string with @page rules for both orientations
     */
    public static String generateMixedOrientationStyles(String pageSize, String margins) {
        return String.format(
                "@page :portrait { size: %s portrait; margin: %s; } " +
                "@page :landscape { size: %s landscape; margin: %s; }",
                pageSize, margins, pageSize, margins
        );
    }

    /**
     * Generates CSS @page rules for both portrait and landscape pages with default margins.
     *
     * @param pageSize the page size (e.g., "A4", "letter")
     * @return a CSS string with @page rules for both orientations
     */
    public static String generateMixedOrientationStyles(String pageSize) {
        return generateMixedOrientationStyles(pageSize, "2cm");
    }
}

