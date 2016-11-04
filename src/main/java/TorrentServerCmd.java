import exceptions.TorrentException;
import models.torrent.TorrentServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TorrentServerCmd {
    public static void main(String[] args) {
        TorrentServer ts = new TorrentServerImpl();

        try {
            ServerSocket server = new ServerSocket(ts.getServerPort());
            while (true) {
                try {
                    System.out.println("waiting new client");
                    Socket socket = server.accept();
                    ts.acceptServerSocket(socket);
                }
                catch (TorrentException e) {
                    System.out.println("TorrentException: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new TorrentException("IOException: ", e);
        }
    }
}
