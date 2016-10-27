package models.requests.server;

import models.requests.Request;

import java.io.DataOutputStream;
import java.io.IOException;


public class UploadRequest implements Request {
    private final String name;
    private final long size;

    public UploadRequest(String name, long size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public String toString() {
        return "2 " + name + " " + size;
    }

    @Override
    public void writeToDataOutputStream(DataOutputStream dos) throws IOException {
        dos.writeByte(2);
        dos.writeUTF(name);
        dos.writeLong(size);
    }
}
