package models.commands.server;

import exceptions.TorrentException;
import models.commands.Command;
import models.torrent.TorrentServerState;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerCommandBuilder {
    public static Command build(TorrentServerState tss, DataInputStream dis, byte[] clientIp) throws IOException {
        byte commandId = dis.readByte();
        switch (commandId) {
            case 1:
                return new ListCommand(tss);
            case 2:
                String fileName = dis.readUTF();
                long fileSize = dis.readLong();
                return new UploadCommand(tss, fileName, fileSize);
            case 3:
                int fileId = dis.readInt();
                return new SourceCommand(tss, fileId);
            case 4:
                short clientPort = dis.readShort();
                int filesCount = dis.readInt();
                Set<Integer> distributedFiles = IntStream.range(0, filesCount).mapToObj((x) -> {
                    try {
                        return dis.readInt();
                    } catch (IOException e) {
                        throw new TorrentException("Cannot read file id from stream", e);
                    }
                }).collect(Collectors.toSet());
                return new UpdateCommand(tss, clientIp, clientPort, distributedFiles);
            default:
                throw new TorrentException("Unexpected command id");
        }
    }
}
