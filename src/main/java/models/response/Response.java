package models.response;


import java.io.DataInputStream;
import java.io.IOException;

public interface Response {
    void readFromDataInputStream(DataInputStream dis) throws IOException;
}
