package models.commands.client;

import models.TorrentFile;
import models.commands.Command;
import models.torrent.TorrentClientState;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class GetCommand implements Command {
    private final TorrentClientState tcs;
    private final int fileId;
    private final int partId;

    public GetCommand(TorrentClientState tcs, int fileId, int partId) {
        this.tcs = tcs;
        this.fileId = fileId;
        this.partId = partId;
    }

    @Override
    public void evaluateCommand(DataOutputStream dos) throws IOException {
        TorrentFile targetFile = tcs.getOwnFiles().get(fileId);
        if (targetFile != null) {
            try (RandomAccessFile raf = new RandomAccessFile(targetFile.getName(), "rw")) {
                raf.seek(partId * targetFile.getPieceSize());
                byte[] content = new byte[targetFile.getPieceSize()];
                int readed = raf.read(content);
                dos.write(content, 0, readed);
                System.out.println("get sended file=" + fileId + "; part=" + partId + ";readed=" + readed);
            }
        }
    }
}
