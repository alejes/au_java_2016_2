package models.command;

import exceptions.FTPException;
import models.FtpFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand extends Command {
    protected ListCommand(String path) {
        super(path);
    }

    @Override
    public String evaluateCommand() {
        Path targetPath = Paths.get(System.getProperty("user.dir")).resolve(path).normalize().toAbsolutePath();
        Path basePath = Paths.get(System.getProperty("user.dir"));
        if (!targetPath.startsWith(basePath)) {
            throw new FTPException("You cant read outside root directory");
        }

        List<String> content = Arrays.stream(new File(targetPath.toString()).listFiles())
                .map(it -> new FtpFile(it.isDirectory(), it.getPath().replace(basePath.toString(), "")))
                .map(FtpFile::toNetworkResponse).collect(Collectors.toList());
        return content.size() +
                content.stream().reduce("", (x, y) -> x + " " + y);
    }

}
