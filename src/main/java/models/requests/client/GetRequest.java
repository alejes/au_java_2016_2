package models.requests.client;

import models.requests.Request;

import java.io.DataOutputStream;
import java.io.IOException;


public class GetRequest implements Request {
    private final int fileId;
    private final int partId;

    public GetRequest(int fileId, int partId) {
        this.fileId = fileId;
        this.partId = partId;
    }

    @Override
    public void writeToDataOutputStream(DataOutputStream dos) throws IOException {
        dos.writeByte(2);
        dos.writeInt(fileId);
        dos.writeInt(partId);
    }

    @Override
    public byte getCommandId() {
        return 2;
    }
}
