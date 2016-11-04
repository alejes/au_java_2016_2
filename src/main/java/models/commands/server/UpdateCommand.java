package models.commands.server;


import models.TorrentPeer;
import models.commands.Command;
import models.torrent.TorrentServerState;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class UpdateCommand implements Command {
    private final TorrentServerState tss;
    private final short clientPort;
    private final Set<Integer> distributedFiles;
    private final byte[] clientIp;

    protected UpdateCommand(TorrentServerState tss, byte[] clientIp, short clientPort, Set<Integer> distributedFiles) {
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
        System.out.println("update info on " + peerCandidate);
        distributedFiles.forEach(x -> System.out.print(x + ";"));
        System.out.println();
        dos.writeBoolean(true);
        dos.flush();
    }
}
