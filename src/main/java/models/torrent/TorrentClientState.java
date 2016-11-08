package models.torrent;


import com.sun.istack.internal.NotNull;
import exceptions.TorrentException;
import models.TorrentFile;

import java.io.*;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class TorrentClientState {
    private final Map<Integer, TorrentFile> ownFiles = new HashMap<>();
    @NotNull
    private ServerSocket server;
    private int clientId;

    public TorrentClientState(@NotNull ServerSocket server, int clientId) {
        this.server = server;
        this.clientId = clientId;

        FileInputStream fis;

        File file = new File("torrent-client-" + clientId + ".dat");
        if (!file.exists()) return;
        try {
            fis = new FileInputStream(file);
        } catch (IOException e) {
            throw new TorrentException("IOException", e);
        }

        try (DataInputStream dis = new DataInputStream(fis)) {
            int size = dis.readInt();
            for (int i = 0; i < size; ++i) {
                TorrentFile torrentFile = new TorrentFile(dis);
                ownFiles.put(torrentFile.getFileId(), torrentFile);
            }
        } catch (IOException e) {
            throw new TorrentException("IOException", e);
        }
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

    public void saveState() {
        FileOutputStream fos;

        File file = new File("torrent-client-" + clientId + ".dat");
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
        } catch (IOException e) {
            throw new TorrentException("IOException", e);
        }

        try (DataOutputStream dos = new DataOutputStream(fos)) {
            dos.writeInt(ownFiles.size());
            for (TorrentFile tf : ownFiles.values()) {
                tf.dumpToDataStream(dos);
            }
        } catch (IOException e) {
            throw new TorrentException("IOException", e);
        }
    }
}
