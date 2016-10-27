import models.torrent.TorrentClient;

public class TorrentClientCmd {
    private static final String serverHost = "127.0.0.1";
    private static final int serverPort = 8081;

    public static void main(String[] args) {
        TorrentClient tc = new TorrentClientImpl(serverHost, serverPort);


    }
}
