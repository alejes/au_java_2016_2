package models;


import java.util.ArrayList;
import java.util.List;

public class TorrentFile {
    private final long size;
    private final String name;
    private final int fileId;
    private final List<Integer> pieces;

    public TorrentFile(int fileId, String name, long size) {
        this.fileId = fileId;
        this.size = size;
        this.name = name;
        this.pieces = new ArrayList<>();
    }

    public TorrentFile(int fileId, long size, String name, List<Integer> pieces) {
        this.fileId = fileId;
        this.size = size;
        this.name = name;
        this.pieces = pieces;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public List<Integer> getPieces() {
        return pieces;
    }

    public int getFileId() {
        return fileId;
    }
}
