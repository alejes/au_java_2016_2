package models.torrent;

import models.TorrentFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface TorrentClient {
    default short getServerPort(){
        return 8081;
    }
    void forceUpdate();
    List<TorrentFile> listFiles();
    void shutdown() throws IOException;
    Collection<TorrentFile> distributedFiles();
    void registerFile(File file);
}
