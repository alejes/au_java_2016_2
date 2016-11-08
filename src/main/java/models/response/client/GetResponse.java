package models.response.client;

import models.response.Response;

import java.io.DataInputStream;
import java.io.IOException;

public class GetResponse implements Response {
    private final byte[] content;

    public GetResponse(int contentSize) {
        content = new byte[contentSize];
    }

    public int getContentSize() {
        return content.length;
    }

    public byte[] getContent() {
        return content;
    }

    @Override
    public void readFromDataInputStream(DataInputStream dis) throws IOException {
        dis.readFully(content);
    }
}
