package models.requests.server;

import models.requests.Request;

import java.io.DataOutputStream;
import java.io.IOException;

public class ListRequest implements Request {
    @Override
    public String toString() {
        return "1";
    }

    @Override
    public void writeToDataOutputStream(DataOutputStream dos) throws IOException {
        dos.writeByte(1);
    }
}
