package models.commands;


import exceptions.TorrentException;
import models.torrent.TorrentServerState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Command {
    public static Command build(TorrentServerState tss, DataInputStream dis) throws IOException {
        byte commandId = dis.readByte();
        switch(commandId){
            case 1:
                return new ListCommand(tss);
            default:
                throw new TorrentException("Unexpected command id");
        }
    }

    public abstract void evaluateCommand(DataOutputStream dos) throws IOException;
}
