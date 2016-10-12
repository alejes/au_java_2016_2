package models.command;


import exceptions.FTPException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GetCommand extends Command {
    protected GetCommand(String path) {
        super(path);
    }

    @Override
    public String evaluateCommand() {
        Path targetPath = Paths.get(System.getProperty("user.dir")).resolve(path).normalize().toAbsolutePath();
        Path basePath = Paths.get(System.getProperty("user.dir"));
        if (!targetPath.startsWith(basePath)) {
            throw new FTPException("You cant read outside root directory");
        }


        File file = new File(targetPath.toString());
        if (!file.exists()) {
            return "0";
        } else {
            try {
                return file.length() + " " + new String(Files.readAllBytes(targetPath));
            } catch (IOException e) {
                return "0";
            }
        }
    }
}
