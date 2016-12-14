package managers;

import exceptions.PerformanceArchitectureException;
import proto.ServerInitMessageOuterClass.ServerInitMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerManager {
    public static final int SERVER_MANAGER_PORT = 50028;
    private final ServerSocket sc = new ServerSocket(SERVER_MANAGER_PORT);
    private final Thread serverThread;
    private boolean shutdown = false;

    public ServerManager() throws IOException {
        ServerWorker serverWorker = new ServerWorker(sc);
        serverThread = new Thread(serverWorker);
        serverThread.start();
    }

    public int getPort() {
        return sc.getLocalPort();
    }

    public void stop() throws IOException, InterruptedException {
        shutdown = true;
        sc.close();
        serverThread.interrupt();
        serverThread.join();
    }

    private class ServerWorker implements Runnable {
        private final ServerSocket sc;

        public ServerWorker(ServerSocket sc) {
            this.sc = sc;
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try (Socket socket = sc.accept()) {
                    try (DataInputStream dis = new DataInputStream(socket.getInputStream());
                         DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                        ServerInitMessage serverInit = ServerInitMessage.parseFrom(dis);
                        System.out.println(serverInit.getStategy().toString());
                    }
                } catch (SocketException e) {
                    if (!shutdown) {
                        throw new PerformanceArchitectureException("Socket error", e);
                    }
                    return;
                } catch (IOException e) {
                    throw new PerformanceArchitectureException("IOException", e);
                }
            }
        }
    }

}
