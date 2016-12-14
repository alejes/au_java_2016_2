package exceptions;

public class PerformanceArchitectureException extends RuntimeException {
    public PerformanceArchitectureException(String message, Throwable cause) {
        super(message, cause);
    }

    public PerformanceArchitectureException(String message) {
        super(message);
    }
}
