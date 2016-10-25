package exceptions;


public class TorrentException    extends RuntimeException {

    public TorrentException(String str) {
        super(str);
    }

    public TorrentException(String str, Throwable cause) {
        super(str, cause);
    }

}
