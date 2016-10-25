package models.torrent;


import java.io.IOException;
import java.net.Socket;

public interface TorrentServer {
    void acceptServerSocket(Socket s) throws IOException;
}
