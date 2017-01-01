package models.commands.server;


import exceptions.TorrentException;
import models.TorrentPeer;
import models.commands.Command;
import models.torrent.TorrentServerState;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SourceCommand implements Command {
    private final TorrentServerState tss;
    private final int fileId;

    protected SourceCommand(TorrentServerState tss, int fileId) {
        this.tss = tss;
        this.fileId = fileId;
    }

    @Override
    public void evaluateCommand(DataOutputStream dos) throws IOException {
        List<TorrentPeer> targetList = tss.getPeersFilesMap().entrySet().stream()
                .filter((entry) -> entry.getValue().contains(fileId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        dos.writeInt(targetList.size());
        targetList.forEach((x) -> {
            try {
                dos.write(x.getPeerIp());
                dos.writeShort(x.getPeerPort());
            } catch (IOException e) {
                throw new TorrentException("Cannot write distributed file id ", e);
            }
        });
    }
}
