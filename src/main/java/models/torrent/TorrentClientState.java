package models.torrent;


import models.TorrentFile;

import java.util.HashMap;
import java.util.Map;

public class TorrentClientState {
    private final Map<Integer, TorrentFile> ownFiles = new HashMap<>();

    public Map<Integer, TorrentFile> getOwnFiles() {
        return ownFiles;
    }
}
