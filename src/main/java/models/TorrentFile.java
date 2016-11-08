package models;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TorrentFile {
    private final long size;
    private final int fileId;
    private final List<Integer> pieces;
    private String name;

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

    public TorrentFile(int fileId, String name, long size, boolean loaded) {
        this.fileId = fileId;
        this.size = size;
        this.name = name;
        if (loaded) {
            this.pieces = IntStream.rangeClosed(0, getTotalPieces())
                    .mapToObj(Integer::new).collect(Collectors.toList());
        } else {
            this.pieces = new ArrayList<>();
        }
    }

    public TorrentFile(DataInputStream dis) throws IOException {
        fileId = dis.readInt();
        name = dis.readUTF();
        size = dis.readLong();
        pieces = new ArrayList<>();
        int piecesSize = dis.readInt();
        for (int i = 0; i < piecesSize; ++i) {
            pieces.add(dis.readInt());
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getCountPieces() {
        return pieces.size();
    }

    public int getTotalPieces() {
        return (int) (size / getPieceSize() + ((size % getPieceSize() != 0) ? 1 : 0));
    }

    public final int getPieceSize() {
        return 10;
    }

    public int getPieceSizeById(int partId) {
        if (partId < getCountPieces() - 1) {
            return getPieceSize();
        } else {
            return (int) size % getPieceSize();
        }
    }

    public boolean isDownload() {
        return getCountPieces() * getPieceSize() >= size;
    }

    public List<Integer> getMissingPieces() {
        return IntStream.range(0, getTotalPieces()).filter(x -> !pieces.contains(x))
                .mapToObj(Integer::new).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String result = getFileId() + "\t" + getName() + "\t" + getSize();
        if (isDownload()) {
            result += "\t" + "[OK]";
        } else {
            result += "\t" + "[" + getCountPieces() + "/" + getTotalPieces() + "]";
        }
        return result;
    }

    public String toString(boolean inLocalList) {
        String result = getFileId() + "\t" + getName() + "\t" + getSize();
        if (!inLocalList) {
            result += "\t" + "[-]";
        } else if (isDownload()) {
            result += "\t" + "[OK]";
        } else {
            result += "\t" + "[" + getCountPieces() + "/" + getTotalPieces() + "]";
        }
        return result;
    }


    public void dumpToDataStream(DataOutputStream dos) throws IOException {
        dos.writeInt(getFileId());
        dos.writeUTF(getName());
        dos.writeLong(getSize());
        dos.writeInt(pieces.size());
        for (Integer piece : pieces) {
            dos.writeInt(piece);
        }
    }
}
