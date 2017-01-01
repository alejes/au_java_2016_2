package models.response.server;


import models.TorrentFile;
import models.response.Response;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListResponse implements Response {
    private final List<TorrentFile> filesList = new ArrayList<>();

    public List<TorrentFile> getFilesList() {
        return filesList;
    }

    @Override
    public void readFromDataInputStream(DataInputStream dis) throws IOException {
        int filesCount = dis.readInt();

        for (int i = 0; i < filesCount; ++i) {
            int fileId = dis.readInt();
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            filesList.add(new TorrentFile(fileId, fileName, fileSize));
        }
    }
}
