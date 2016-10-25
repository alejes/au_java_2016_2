import models.commands.Command;
import models.torrent.TorrentServer;
import models.torrent.TorrentServerState;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class TorrentServerImpl implements TorrentServer {
    private final TorrentServerState tss = new TorrentServerState();

    @Override
    public void acceptServerSocket(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        DataInputStream dis = new DataInputStream(is);
        OutputStream os = socket.getOutputStream();

        while (socket.isConnected() && !socket.isInputShutdown()) {
            Command cmd = Command.build(tss, dis);
        }
    }
}
