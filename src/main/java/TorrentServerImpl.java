import models.commands.Command;
import models.commands.server.ServerCommandBuilder;
import models.torrent.TorrentServer;
import models.torrent.TorrentServerState;

import java.io.*;
import java.net.Socket;


public class TorrentServerImpl implements TorrentServer {
    private final TorrentServerState tss = new TorrentServerState();

    @Override
    public void acceptServerSocket(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        DataInputStream dis = new DataInputStream(is);
        OutputStream os = socket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);

        while (socket.isConnected() && !socket.isInputShutdown()) {
            byte[] ip = socket.getInetAddress().getAddress();
            Command cmd = ServerCommandBuilder.build(tss, dis, ip);
            cmd.evaluateCommand(dos);
            socket.close();
        }
    }
}
