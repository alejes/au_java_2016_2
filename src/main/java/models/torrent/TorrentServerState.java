package models.torrent;


import models.TorrentFile;
import models.TorrentPeer;

import java.util.*;

public class TorrentServerState {
    private final List<TorrentFile> listTorrentFiles = new ArrayList<>();
    private final Map<TorrentPeer, Set<Integer>> peersFilesMap = new HashMap<>();

    public Map<TorrentPeer, Set<Integer>> getPeersFilesMap() {
        return peersFilesMap;
    }

    public List<TorrentFile> getListTorrentFiles() {
        return listTorrentFiles;
    }
}
