package models.torrent;

import models.TorrentFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface TorrentClient {
    default short getServerPort() {
        return 8081;
    }

    void forceUpdate();

    List<TorrentFile> listFiles();

    boolean addGetTask(int id, String location);

    void shutdown() throws IOException;

    List<TorrentFile> distributedFiles();

    void registerFile(File file);
}
