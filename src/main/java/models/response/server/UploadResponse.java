package models.response.server;


import models.response.Response;

import java.io.DataInputStream;
import java.io.IOException;

public class UploadResponse implements Response {
    private int fileId;

    public int getFileId() {
        return fileId;
    }

    @Override
    public void readFromDataInputStream(DataInputStream dis) throws IOException {
        fileId = dis.readInt();
    }
}
