import exceptions.TorrentException;
import models.torrent.TorrentServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TorrentServerCmd {
    private static Thread workedThread;
    private static ServerSocket server;

    public static void main(String[] args) {
        TorrentServer ts = new TorrentServerImpl();

        workedThread = new Thread(() -> {
            try {
                server = new ServerSocket(ts.getServerPort());
                while (!Thread.interrupted()) {
                    try {
                        System.out.println("waiting new client");
                        Socket socket = server.accept();
                        if (Thread.interrupted()) {
                            break;
                        }
                        ts.acceptServerSocket(socket);
                    } catch (TorrentException e) {
                        System.out.println("TorrentException: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                if (!Thread.interrupted()) {
                    System.out.println("IOException: " + e.getMessage());
                }
            }
        });
        workedThread.start();

        Scanner scr = new Scanner(System.in);
        boolean activeConnection = true;
        while (activeConnection) {
            String mode = scr.next();
            switch (mode) {
                case "exit":
                    activeConnection = false;
                    workedThread.interrupt();
                    try {
                        server.close();
                    } catch (IOException e) {
                        System.out.println("Cannot close socket: " + e.getMessage());
                    }
                    try {
                        ts.shutdown();
                    } catch (TorrentException e) {
                        System.out.println("Cannot close torrent server: " + e.getMessage());
                    }

                    break;
                default:
                    System.out.println("Unknown command = " + mode);
            }

        }
    }
}
