import exceptions.TorrentException;
import models.torrent.TorrentServer;

import java.io.Closeable;
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

        try (ServerManager serverManager = new ServerManager(cleanState)) {
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
        }
    }

    public static class ServerManager implements Closeable {
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

        public void close() {
            workedThread.interrupt();
            ts.shutdown();
            if (!server.isClosed()) {
                try {
                    server.close();
                } catch (IOException e) {
                    throw new TorrentException("IOException", e);
                }
            }
        }

        private class WorkedThread implements Runnable {
            @Override
            public void run() {
                try {
                    server = new ServerSocket(ts.getServerPort());
                    while (!Thread.interrupted()) {
                        System.out.println("server - waiting new client");
                        try (Socket socket = server.accept()) {
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