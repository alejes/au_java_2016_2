import models.TorrentFile;
import models.torrent.TorrentClient;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Scanner;

public class TorrentClientCmd {
    private static final String serverHost = "127.0.0.1";

    public static void main(String[] args) {
        try {
            TorrentClient tc = new TorrentClientImpl(serverHost);
            Scanner scr = new Scanner(System.in);
            boolean activeConnection = true;
            while (activeConnection) {
                String mode = scr.next();
                switch (mode) {
                    case "add":
                        String targetFile = scr.next();
                        File file = new File(targetFile);
                        if (!file.exists()) {
                            System.out.println("File not found");
                            break;
                        }
                        tc.registerFile(file);
                        break;
                    case "mylist":
                        System.out.println("Distributed files");
                        tc.distributedFiles().forEach(System.out::println);
                        break;
                    case "list":
                        System.out.println("Global distributed files");
                        Collection<TorrentFile> localList = tc.distributedFiles();
                        tc.listFiles().forEach(x -> {
                            Optional<TorrentFile> local = localList.stream().filter(y -> y.getFileId() == x.getFileId()).findAny();
                            if (local.isPresent()) {
                                System.out.println(local.get().toString(true));
                            } else {
                                System.out.println(x.toString(false));
                            }
                        });
                        break;
                    case "update":
                        System.out.println("Force update");
                        tc.forceUpdate();
                        break;
                    case "load":
                        int sourcesId = scr.nextInt();
                        System.out.println("Target file location");
                        String location = scr.next();
                        if (tc.addGetTask(sourcesId, location)) {
                            System.out.println("file successfully added to download");
                        } else {
                            System.out.println("file added failed, maybe it already in queue");
                        }
                        break;
                    case "exit":
                        tc.shutdown();
                        activeConnection = false;
                        break;
                    default:
                        System.out.println("Unknown command = " + mode);
                }
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }


    }
}
