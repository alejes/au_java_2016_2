package models.command;

import exceptions.FTPException;
import models.FtpFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand extends Command {
    protected ListCommand(DataInputStream dis) throws IOException {
        super(dis);
    }

    @Override
    public void evaluateCommand(DataOutputStream dos) throws IOException {
        Path basePath = Paths.get(System.getProperty("user.dir"));
        Path targetPath = basePath.resolve(path).normalize().toAbsolutePath();

        if (!targetPath.startsWith(basePath)) {
            throw new FTPException("You cant read outside root directory");
        }

        File[] fileList = new File(targetPath.toString()).listFiles();
        if (fileList == null) {
            dos.writeLong(0);
            return;
        }

        List<FtpFile> content = Arrays.stream(fileList)
                .map(it -> new FtpFile(it.isDirectory(), it.getPath().replace(basePath.toString(), "")))
                .collect(Collectors.toList());

        dos.writeInt(content.size());

        for (FtpFile item : content) {
            dos.writeUTF(item.getName());
            dos.writeBoolean(item.isDirectory());
        }
    }

}
