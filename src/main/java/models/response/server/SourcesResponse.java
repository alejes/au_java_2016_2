package models.response.server;


import exceptions.TorrentException;
import models.TorrentPeer;
import models.response.Response;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SourcesResponse implements Response {
    private List<TorrentPeer> peersList = new ArrayList<>();

    public List<TorrentPeer> getPeersList() {
        return peersList;
    }

    @Override
    public void readFromDataInputStream(DataInputStream dis) throws IOException {
        int filesCount = dis.readInt();

        for (int i = 0; i < filesCount; ++i) {
            byte[] clientIp = new byte[4];
            int result = dis.read(clientIp, 0, 4);
            if (result != 4) {
                throw new TorrentException("Cannot read ip from stream");
            }
            short clientPort = dis.readShort();
            peersList.add(new TorrentPeer(clientIp, clientPort));
        }
    }
}
