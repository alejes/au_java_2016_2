import models.torrent.TorrentClient;

import java.io.IOException;

public class TorrentClientCmd {
    private static final String serverHost = "127.0.0.1";

    public static void main(String[] args) {
        try {
            TorrentClient tc = new TorrentClientImpl(serverHost);

        } catch (IOException e) {
            System.out.println("IOException: "+ e.getMessage());
        }


    }
}
