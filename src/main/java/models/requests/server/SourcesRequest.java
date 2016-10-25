package models.requests.server;

import models.requests.Request;

import java.io.DataOutputStream;
import java.io.IOException;

public class SourcesRequest implements Request {
    private int fileId;

    public SourcesRequest(int fileId) {
        this.fileId = fileId;
    }

    @Override
    public String toString() {
        return "3 " + fileId;
    }

    @Override
    public void writeToDataOutputStream(DataOutputStream dos) throws IOException {
        dos.writeByte(3);
        dos.writeInt(fileId);
    }
}
