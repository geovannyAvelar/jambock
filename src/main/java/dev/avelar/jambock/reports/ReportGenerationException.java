package dev.avelar.jambock.reports;

/**
 * Exception thrown when there's an error generating a report.
 */
public class ReportGenerationException extends Exception {

    /**
     * Creates a new ReportGenerationException with the specified message.
     *
     * @param message the error message
     */
    public ReportGenerationException(String message) {
        super(message);
    }

    /**
     * Creates a new ReportGenerationException with the specified message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

