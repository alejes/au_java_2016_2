package models.requests;


import java.io.DataOutputStream;
import java.io.IOException;

public class ListRequest {
    private final String path;

    public ListRequest(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "1 " + path;
    }

    public void dump(DataOutputStream dos) throws IOException {
        dos.writeInt(1);
        dos.writeUTF(path);
    }
}
