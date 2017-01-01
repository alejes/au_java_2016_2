package models.torrent;

import models.TorrentFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface TorrentClient extends Closeable {
    default short getServerPort() {
        return 8081;
    }

    boolean forceUpdate();

    List<TorrentFile> listFiles();

    boolean addGetTask(int id, String location);

    void close() throws IOException;

    List<TorrentFile> distributedFiles();

    void registerFile(File file);
}
