package server;

import models.commands.Command;
import models.commands.server.ServerCommandBuilder;
import models.torrent.TorrentServer;
import models.torrent.TorrentServerState;

import java.io.*;
import java.net.Socket;


public class TorrentServerImpl implements TorrentServer {
    private final TorrentServerState tss;

    public TorrentServerImpl(boolean cleanState) {
        tss = new TorrentServerState(cleanState);
    }

    @Override
    public void shutdown() {
        System.out.println("server go offline");
        tss.saveState();
    }

    @Override
    public void acceptServerSocket(Socket socket) throws IOException {
        try (
                InputStream is = socket.getInputStream();
                DataInputStream dis = new DataInputStream(is);
                OutputStream os = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os)
        ) {
            if (socket.isConnected() && !socket.isInputShutdown()) {
                byte[] ip = socket.getInetAddress().getAddress();
                Command cmd = ServerCommandBuilder.build(tss, dis, ip);
                cmd.evaluateCommand(dos);
            }
        }
    }
}
