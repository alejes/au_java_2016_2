package models.command;


import exceptions.FTPException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;


public class GetCommand extends Command {
    protected GetCommand(DataInputStream dis) throws IOException {
        super(dis);
    }

    @Override
    public void evaluateCommand(DataOutputStream dos) throws IOException, FTPException {
        Path basePath = Paths.get(System.getProperty("user.dir"));
        Path targetPath = basePath.resolve(path).normalize().toAbsolutePath();
        if (!targetPath.startsWith(basePath)) {
            throw new FTPException("You cant read outside root directory");
        }

        File file = targetPath.toFile();
        if (!file.exists() || file.isDirectory()) {
            dos.writeLong(0);
        } else {
            dos.writeLong(file.length());
            try (FileInputStream fis = new FileInputStream(file)) {
                long readed = 0;
                byte[] buffer = new byte[4096];
                while (readed < file.length()) {
                    int readCnt = fis.read(buffer);
                    if (readCnt > 0) {
                        dos.write(buffer, 0, readCnt);
                        readed += readCnt;
                    }
                }
            }
        }
    }
}
