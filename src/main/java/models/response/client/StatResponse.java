package models.response.client;

import models.response.Response;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StatResponse implements Response {
    private List<Integer> partsList = new ArrayList<>();

    public List<Integer> getPartsList() {
        return partsList;
    }

    @Override
    public void readFromDataInputStream(DataInputStream dis) throws IOException {
        int filesCount = dis.readInt();

        for (int i = 0; i < filesCount; ++i) {
            int partId = dis.readInt();
            partsList.add(partId);
        }
    }
}
