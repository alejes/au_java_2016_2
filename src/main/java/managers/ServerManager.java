package managers;

import builders.ServerBuilder;
import exceptions.PerformanceArchitectureException;
import proto.ServerInitMessageOuterClass.ServerInitMessage;
import proto.ServerRequestStatMessageOuterClass.ServerRequestStatMessage;
import proto.ServerResponseStatMessageOuterClass.ServerResponseStatMessage;
import servers.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerManager {
    public static final String SERVER_MANAGER_HOST = "127.0.0.1";
    public static final int SERVER_MANAGER_PORT = 50028;
    private final ServerSocket sc = new ServerSocket(SERVER_MANAGER_PORT);
    private final Thread serverThread;
    private boolean shutdown = false;

    public ServerManager() throws IOException {
        ServerManagerWorker serverManagerWorker = new ServerManagerWorker(sc);
        serverThread = new Thread(serverManagerWorker);
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

    private class ServerManagerWorker implements Runnable {
        private final ServerSocket sc;

        public ServerManagerWorker(ServerSocket sc) {
            this.sc = sc;
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try (Socket socket = sc.accept()) {
                    try (DataInputStream dis = new DataInputStream(socket.getInputStream());
                         DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                        ServerInitMessage serverInit = ServerInitMessage.parseDelimitedFrom(dis);
                        System.out.println(serverInit.getStategy().toString());
                        Server srv = ServerBuilder.buildServer(serverInit.getStategy());
                        Thread t = new Thread(srv);
                        t.start();
                        srv.getServerData().writeDelimitedTo(dos);
                        dos.flush();
                        ServerRequestStatMessage.parseDelimitedFrom(dis);
                        ServerResponseStatMessage stat = srv.stopAndCollectStatistic();
                        stat.writeDelimitedTo(dos);
                        dos.flush();
                        t.interrupt();
                        t.join();
                    } catch (InterruptedException e) {
                        throw new PerformanceArchitectureException("Interrupted error", e);
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
