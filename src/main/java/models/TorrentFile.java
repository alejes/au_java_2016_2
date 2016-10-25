package models;


import java.util.ArrayList;
import java.util.List;

public class TorrentFile {
    private int fileId;
    private List<Integer> pieces;

    public TorrentFile(int fileId) {
        this.fileId = fileId;
        this.pieces = new ArrayList<>();
    }

    public TorrentFile(int fileId, List<Integer> pieces) {
        this.fileId = fileId;
        this.pieces = pieces;
    }

    public List<Integer> getPieces() {
        return pieces;
    }

    public int getFileId() {
        return fileId;
    }
}
