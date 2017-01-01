package exceptions;

public class FTPException extends Exception {

    public FTPException(String str) {
        super(str);
    }

    public FTPException(String str, Throwable cause) {
        super(str, cause);
    }
}