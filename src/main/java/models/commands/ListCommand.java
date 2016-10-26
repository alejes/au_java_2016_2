package models.commands;


import exceptions.TorrentException;
import models.torrent.TorrentServerState;

import java.io.DataOutputStream;
import java.io.IOException;


public class ListCommand extends Command {
    private final TorrentServerState tss;

    protected ListCommand(TorrentServerState tss) {
        this.tss = tss;
    }

    @Override
    public void evaluateCommand(DataOutputStream dos) throws IOException {
        dos.writeInt(tss.getListTorrentFiles().size());
        tss.getListTorrentFiles().forEach((x) -> {
            try {
                dos.writeInt(x.getFileId());
                dos.writeUTF(x.getName());
                dos.writeLong(x.getSize());
            } catch (IOException e) {
                throw new TorrentException("Cannot write distributed file id " + x.getFileId(), e);
            }
        });
    }
}
