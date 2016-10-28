package models.torrent;


import com.sun.istack.internal.NotNull;
import models.TorrentFile;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class TorrentClientState {
    private final Map<Integer, TorrentFile> ownFiles = new HashMap<>();
    @NotNull
    private ServerSocket server;

    public TorrentClientState(@NotNull ServerSocket server) {
        this.server = server;
    }

    public ServerSocket getServer() {
        return server;
    }

    public Map<Integer, TorrentFile> getOwnFiles() {
        return ownFiles;
    }

    public short getPort() {
        return (short) (Short.MAX_VALUE - server.getLocalPort());
    }
}
