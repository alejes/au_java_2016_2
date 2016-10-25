package models.requests.server;

import exceptions.TorrentException;
import models.TorrentFile;
import models.requests.Request;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class UpdateRequest implements Request {

    private short clientPort;

    private List<TorrentFile> distributedFiles;

    @Override
    public String toString() {
        return super.toString();
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
