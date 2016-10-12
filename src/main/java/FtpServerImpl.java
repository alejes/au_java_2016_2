import exceptions.FTPException;
import ftp.Ftp;
import ftp.FtpServer;
import models.command.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FtpServerImpl extends Ftp implements FtpServer {
    private final static int bufferLength = 4096;
    private final int serverPort = 8000;
    private Thread serverManager = null;

    @Override
    public void serverStart() {
        FtpServerManager fsm = new FtpServerManager(serverPort);
        serverManager = new Thread(fsm);
        serverManager.start();
    }

    @Override
    public void serverStop() {
        if (serverManager != null) {
            serverManager.interrupt();
        }
    }

    private static class FtpServerManager implements Runnable {
        private final int serverPort;

        public FtpServerManager(int serverPort) {
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            try {
                ServerSocket server = new ServerSocket(serverPort);
                while (true) {
                    System.out.println("waiting new client");
                    Socket socket = server.accept();
                    FtpWorker fw = new FtpWorker(socket);
                    new Thread(fw).start();
                }
            } catch (IOException e) {
                throw new FTPException("IOException: ", e);
            }
        }
    }

    private static class FtpWorker implements Runnable {
        private final Socket socket;
        private final byte[] requestBytes = new byte[bufferLength];

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
                    int bytesRead = is.read(requestBytes);
                    if (bytesRead < 0){
                        continue;
                    }
                    String request = new String(requestBytes).substring(0, bytesRead);
                    Command cmd = Command.build(request);
                    try {
                        String response = cmd.evaluateCommand();
                        os.write(response.getBytes());
                    } catch (Exception exception) {
                        os.write("0".getBytes());
                        throw exception;
                    } finally {
                        os.flush();
                    }
                }
                System.out.println("The client disconnected");
            } catch (IOException e) {
                throw new FTPException("IOException: ", e);
            }
        }
    }
}
