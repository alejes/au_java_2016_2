package models.commands.client;

import exceptions.TorrentException;
import models.commands.Command;
import models.torrent.TorrentClientState;

import java.io.DataInputStream;
import java.io.IOException;


public class ClientCommandBuilder {
    public static Command build(TorrentClientState tcs, DataInputStream dis) throws IOException {
        byte commandId = dis.readByte();
        switch (commandId) {
            case 1:
                int fileId = dis.readInt();
                return new StatCommand(tcs, fileId);
            case 2:
                int getFileId = dis.readInt();
                int pieceId = dis.readInt();
                return new GetCommand(tcs, getFileId, pieceId);
            default:
                throw new TorrentException("Unexpected command id=" + commandId);
        }
    }
}

