package models.requests.client;

import models.requests.Request;

import java.io.DataOutputStream;
import java.io.IOException;


public class GetRequest implements Request {
    private int fileId;
    private int partId;

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void writeToDataOutputStream(DataOutputStream dos) throws IOException {
        dos.writeByte(2);
        dos.writeInt(fileId);
        dos.writeInt(partId);
    }
}
