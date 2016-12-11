package models.requests;


import java.io.DataOutputStream;
import java.io.IOException;

public class GetRequest {
    private final String path;

    public GetRequest(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "2 " + path;
    }

    public void dump(DataOutputStream dos) throws IOException {
                dos.writeInt(2);
                dos.writeUTF(path);
    }

}
