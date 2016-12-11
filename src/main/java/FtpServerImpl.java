import exceptions.FTPException;
import ftp.FtpServer;
import models.command.Command;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class FtpServerImpl implements FtpServer {
    private final int serverPort;
    private Thread serverManager = null;
    private ServerSocket serverSocket;
    private boolean shutdown = false;

    public FtpServerImpl() {
        this(8000);
    }

    public FtpServerImpl(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void serverStart() throws IOException {
        serverSocket = new ServerSocket(serverPort);
        FtpServerManager fsm = new FtpServerManager(serverSocket);
        serverManager = new Thread(fsm);
        serverManager.start();
    }

    @Override
    public void serverStop() throws IOException {
        shutdown = true;
        if (serverManager != null) {
            try {
                serverSocket.close();
            } finally {
                serverManager.interrupt();
            }
        }
    }

    private static class FtpWorker implements Runnable {
        private final Socket socket;

        public FtpWorker(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                System.out.println("run worker for new client");
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                while (socket.isConnected() && !socket.isInputShutdown()) {
                    DataInputStream dis = new DataInputStream(is);
                    if (dis.available() <= 0) {
                        continue;
                    }
                    Command cmd = Command.build(dis);
                    DataOutputStream dos = new DataOutputStream(os);
                    try {
                        cmd.evaluateCommand(dos);
                    } finally {
                        dos.flush();
                        os.flush();
                    }
                }
                System.out.println("The client disconnected");
            } catch (FTPException | IOException e) {
                System.out.println("Exception: " + e.getMessage());
            }
        }
    }

    private class FtpServerManager implements Runnable {
        private ServerSocket server;

        public FtpServerManager(ServerSocket server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    System.out.println("waiting new client");
                    Socket socket = server.accept();
                    FtpWorker fw = new FtpWorker(socket);
                    new Thread(fw).start();
                }
            } catch (SocketException e) {
                if (!shutdown) {
                    System.out.println("SocketException: " + e.getMessage());
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}
