package models.commands;


import models.TorrentPeer;
import models.torrent.TorrentServerState;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class UpdateCommand extends Command {
    private final TorrentServerState tss;
    private final short clientPort;
    private final Set<Integer> distributedFiles;
    private final byte[] clientIp;

    public UpdateCommand(TorrentServerState tss, byte[] clientIp, short clientPort, Set<Integer> distributedFiles) {
        this.tss = tss;
        this.clientPort = clientPort;
        this.distributedFiles = distributedFiles;
        this.clientIp = clientIp;
    }

    @Override
    public void evaluateCommand(DataOutputStream dos) throws IOException {
        Map<TorrentPeer, Set<Integer>> peersMap = tss.getPeersFilesMap();
        TorrentPeer peerCandidate = new TorrentPeer(clientIp, clientPort);
        peersMap.put(peerCandidate, distributedFiles);
        dos.writeBoolean(true);
    }
}
