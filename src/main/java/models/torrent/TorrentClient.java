package models.torrent;

import models.TorrentFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface TorrentClient {
    default short getServerPort(){
        return 8081;
    }
    void update();
    void shutdown() throws IOException;
    Collection<TorrentFile> distributedFiles();
    void registerFile(File file);
}
