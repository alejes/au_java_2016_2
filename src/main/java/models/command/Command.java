package models.command;


import exceptions.FTPException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Command {
    protected final String path;

    protected Command(DataInputStream  dis) throws IOException {
        path = dis.readUTF();
    }

    public static Command build(DataInputStream dis) throws IOException {
        int commandId = dis.readInt();
        switch (commandId) {
            case 1:
                return new ListCommand(dis);
            case 2:
                return new GetCommand(dis);
            default:
                throw new FTPException("Wrong request: " + commandId);
        }
    }

    public abstract void evaluateCommand(DataOutputStream os) throws IOException;
}
