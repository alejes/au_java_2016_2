package models.commands.client;

import exceptions.TorrentException;
import models.TorrentFile;
import models.commands.Command;
import models.torrent.TorrentClientState;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class StatCommand implements Command {
    private final TorrentClientState tcs;
    private final int fileId;

    public StatCommand(TorrentClientState tcs, int fileId) {
        this.tcs = tcs;
        this.fileId = fileId;
    }

    @Override
    public void evaluateCommand(DataOutputStream dos) throws IOException {
        Map<Integer, TorrentFile> filesMap = tcs.getOwnFiles();
        TorrentFile torrentFile = filesMap.get(fileId);
        if (torrentFile == null) {
            dos.writeInt(0);
        } else {
            List<Integer> pieces = torrentFile.getPieces();
            dos.writeInt(pieces.size());
            pieces.forEach((x) -> {
                try {
                    dos.writeInt(x);
                } catch (IOException e) {
                    throw new TorrentException("Cannot write distributed file piece id " + x, e);
                }
            });
        }
    }
}


