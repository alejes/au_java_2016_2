package models.response.server;

import exceptions.TorrentException;
import models.response.Response;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Logger;


public class UpdateResponse implements Response {
    private static Logger log = Logger.getLogger(UpdateResponse.class.getName());

    @Override
    public void readFromDataInputStream(DataInputStream dis) throws IOException {
        boolean result = dis.readBoolean();
        if (!result) {
            throw new TorrentException("Update server information failed");
        }
    }
}
