package models.torrent;


import java.io.IOException;
import java.net.Socket;

public interface TorrentServer {
    default short getServerPort(){
        return 8081;
    }
    void acceptServerSocket(Socket s) throws IOException;
}
