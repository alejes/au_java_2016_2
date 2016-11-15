package models.command;


import exceptions.FTPException;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Command {
    protected final String path;

    protected Command(String path) {
        this.path = path;
    }

    public static Command build(String request) {
        if (request.length() < 3) {
            throw new FTPException("Wrong request: " + request);
        }
        switch (request.charAt(0)) {
            case '1':
                return new ListCommand(request.substring(2));
            case '2':
                return new GetCommand(request.substring(2));
            default:
                throw new FTPException("Wrong request: " + request);
        }
    }

    public abstract void evaluateCommand(DataOutputStream os) throws IOException;
}
