package models.requests;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Request {
    void writeToDataOutputStream(DataOutputStream dos) throws IOException;

    byte getCommandId();
}
