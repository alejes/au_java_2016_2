package models.torrent;


import exceptions.TorrentException;
import models.TorrentFile;
import models.TorrentPeer;

import java.io.*;
import java.util.*;

public class TorrentServerState {
    private final List<TorrentFile> listTorrentFiles = new ArrayList<>();
    private final Map<TorrentPeer, Set<Integer>> peersFilesMap = new HashMap<>();

    public TorrentServerState() {
        FileInputStream fis;

        File file = new File("torrent-settings.dat");
        if (!file.exists()) return;
        try {
            fis = new FileInputStream(file);
        } catch (IOException e) {
            throw new TorrentException("IOException", e);
        }

        try (DataInputStream dis = new DataInputStream(fis)) {
            int size = dis.readInt();
            for (int i = 0; i < size; ++i) {
                int fileId = dis.readInt();
                String fileName = dis.readUTF();
                long fileSize = dis.readLong();
                listTorrentFiles.add(new TorrentFile(fileId, fileName, fileSize));
            }
        } catch (IOException e) {
            throw new TorrentException("IOException", e);
        }
    }

    public Map<TorrentPeer, Set<Integer>> getPeersFilesMap() {
        return peersFilesMap;
    }

    public List<TorrentFile> getListTorrentFiles() {
        return listTorrentFiles;
    }

    public void saveState() {
        FileOutputStream fos;

        File file = new File("torrent-settings.dat");
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
        } catch (IOException e) {
            throw new TorrentException("IOException", e);
        }

        try (DataOutputStream dos = new DataOutputStream(fos)) {
            dos.writeInt(listTorrentFiles.size());
            for (TorrentFile tf : listTorrentFiles) {
                dos.writeInt(tf.getFileId());
                dos.writeUTF(tf.getName());
                dos.writeLong(tf.getSize());
            }
        } catch (IOException e) {
            throw new TorrentException("IOException", e);
        }
    }
}
