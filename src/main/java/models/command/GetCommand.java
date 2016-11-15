package models.command;


import exceptions.FTPException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;


public class GetCommand extends Command {
    protected GetCommand(String path) {
        super(path);
    }

    @Override
    public void evaluateCommand(DataOutputStream dos) throws IOException {
        Path targetPath = Paths.get(System.getProperty("user.dir")).resolve(path).normalize().toAbsolutePath();
        Path basePath = Paths.get(System.getProperty("user.dir"));
        if (!targetPath.startsWith(basePath)) {
            throw new FTPException("You cant read outside root directory");
        }

        File file = new File(targetPath.toString());
        try {
            if (!file.exists() || file.isDirectory()) {
                dos.writeLong(0);
            } else {
                dos.writeLong(file.length());
                dos.writeChar(' ');
                File fl = new File(targetPath.toString());
                FileInputStream fis = new FileInputStream(fl);
                long readed = 0;
                byte[] buffer = new byte[4096];
                while (readed < file.length()) {
                    int readCnt = fis.read(buffer);
                    dos.write(buffer, 0, readCnt);
                    readed += readCnt;
                }
            }
        } catch (IOException e) {
            dos.writeLong(0);
        }
    }
}
