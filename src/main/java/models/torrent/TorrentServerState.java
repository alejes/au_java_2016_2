package models.torrent;


import models.TorrentFile;

import java.util.ArrayList;
import java.util.List;

public class TorrentServerState {
    private final List<TorrentFile> listTorrentFiles = new ArrayList<>();

    public List<TorrentFile> getListTorrentFiles() {
        return listTorrentFiles;
    }
}
