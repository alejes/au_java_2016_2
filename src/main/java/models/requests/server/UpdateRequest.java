package models.requests.server;

import exceptions.TorrentException;
import models.requests.Request;
import models.torrent.TorrentClientState;

import java.io.DataOutputStream;
import java.io.IOException;

public class UpdateRequest implements Request {
    private final TorrentClientState tcs;

    public UpdateRequest(TorrentClientState tcs) {
        this.tcs = tcs;
    }

    @Override
    public void writeToDataOutputStream(DataOutputStream dos) throws IOException {
        dos.writeShort(tcs.getPort());
        tcs.getOwnFiles().values().forEach((x) -> {
            try {
                dos.writeInt(x.getFileId());
            } catch (IOException e) {
                throw new TorrentException("Cannot write distributed file id " + x.getFileId(), e);
            }
        });
    }
}
