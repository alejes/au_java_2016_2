package models.requests.server;

import exceptions.TorrentException;
import models.TorrentFile;
import models.requests.Request;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class UpdateRequest implements Request {

    private final short clientPort;

    private final List<TorrentFile> distributedFiles;

    public UpdateRequest(short clientPort, List<TorrentFile> distributedFiles) {
        this.clientPort = clientPort;
        this.distributedFiles = distributedFiles;
    }

    @Override
    public void writeToDataOutputStream(DataOutputStream dos) throws IOException {
        dos.writeShort(clientPort);
        distributedFiles.forEach((x) -> {
            try {
                dos.writeInt(x.getFileId());
            } catch (IOException e) {
                throw new TorrentException("Cannot write distributed file id " + x.getFileId(), e);
            }
        });
    }
}
