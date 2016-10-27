package models.commands.client;

import models.commands.Command;
import models.torrent.TorrentClientState;

import java.io.DataOutputStream;
import java.io.IOException;

public class GetCommand implements Command {
    private final TorrentClientState tcs;
    private final int fileId;
    private final int partId;

    public GetCommand(TorrentClientState tcs, int fileId, int partId) {
        this.tcs = tcs;
        this.fileId = fileId;
        this.partId = partId;
    }

    @Override
    public void evaluateCommand(DataOutputStream dos) throws IOException {

    }
}
