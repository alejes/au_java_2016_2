import models.torrent.TorrentClient;

import java.io.File;
import java.io.IOException;
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
                        //System.out.println("Enter file location: ");
                        String targetFile = scr.next();
                        File file = new File(targetFile);
                        if (!file.exists()){
                            System.out.println("File not found");
                            break;
                        }
                        tc.registerFile(file);
                        break;
                    case "list":
                        System.out.println("Distributed files");
                        tc.distributedFiles().forEach((x) -> System.out.println(x.getFileId() + "\t" + x.getName() + "\t" + x.getSize() + "\n"));
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
