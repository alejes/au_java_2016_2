import exceptions.TorrentException;
import models.torrent.TorrentServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TorrentServerCmd {
    public static void main(String[] args) {
        boolean cleanState = false;
        if (args.length > 0) {
            cleanState = args[0].equals("cleanState");
        }

        ServerManager serverManager = new ServerManager(cleanState);
        serverManager.start();

        Scanner scr = new Scanner(System.in);
        boolean activeConnection = true;
        while (activeConnection) {
            String mode = scr.next();
            switch (mode) {
                case "exit":
                    activeConnection = false;
                    break;
                default:
                    System.out.println("Unknown command = " + mode);
            }
        }
        serverManager.shutdown();
    }

    public static class ServerManager {
        private static ServerSocket server;
        private final TorrentServer ts;
        private Thread workedThread;

        public ServerManager(boolean cleanState) {
            ts = new TorrentServerImpl(cleanState);
        }

        public void start() {
            workedThread = new Thread(new WorkedThread());
            workedThread.start();
        }

        public void shutdown() {
            workedThread.interrupt();
            ts.shutdown();
            if (!server.isClosed()) {
                try {
                    server.close();
                } catch (IOException e) {
                    System.out.println("IOException: " + e.getMessage());
                }
            }
        }

        private class WorkedThread implements Runnable {
            @Override
            public void run() {
                try {
                    server = new ServerSocket(ts.getServerPort());
                    while (!Thread.interrupted()) {
                        try {
                            System.out.println("server - waiting new client");
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
            }
        }
    }

}