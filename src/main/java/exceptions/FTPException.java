package exceptions;

public class FTPException extends RuntimeException {

    public FTPException(String str) {
        super(str);
    }

    public FTPException(String str, Throwable cause) {
        super(str, cause);
    }
}