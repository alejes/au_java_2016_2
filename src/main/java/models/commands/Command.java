package models.commands;


import java.io.DataOutputStream;
import java.io.IOException;

public interface Command {
    void evaluateCommand(DataOutputStream dos) throws IOException;
}
