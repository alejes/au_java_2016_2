package models.commands.server;


import models.TorrentFile;
import models.commands.Command;
import models.torrent.TorrentServerState;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class UploadCommand implements Command {
    private final TorrentServerState tss;
    private final String fileName;
    private final long fileSize;

    protected UploadCommand(TorrentServerState tss, String fileName, long fileSize) {
        this.tss = tss;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    @Override
    public void evaluateCommand(DataOutputStream dos) throws IOException {
        List<TorrentFile> list = tss.getListTorrentFiles();
        int fileId = list.size();
        tss.getListTorrentFiles().add(new TorrentFile(fileId, fileName, fileSize));
    }
}
