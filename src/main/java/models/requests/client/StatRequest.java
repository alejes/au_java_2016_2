package models.requests.client;

import models.requests.Request;

import java.io.DataOutputStream;
import java.io.IOException;


public class StatRequest implements Request {
    private final int fileId;

    public StatRequest(int fileId) {
        this.fileId = fileId;
    }

    @Override
    public String toString() {
        return "1 " + fileId;
    }

    @Override
    public void writeToDataOutputStream(DataOutputStream dos) throws IOException {
        dos.writeShort(1);
        dos.writeInt(fileId);
    }

    @Override
    public byte getCommandId() {
        return 1;
    }
}
