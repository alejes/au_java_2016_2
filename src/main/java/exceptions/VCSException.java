package exceptions;

public class VCSException extends RuntimeException {

    public VCSException(String str) {
        super(str);
    }

    public VCSException(String str, Throwable cause) {
        super(str, cause);
    }
}
