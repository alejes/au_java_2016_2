package managers;

import builders.ClientBuilder;
import clients.Client;
import exceptions.PerformanceArchitectureException;
import proto.ClientInitMessageOuterClass.ClientInitMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ClientManager {
    public static final String CLIENT_MANAGER_HOST = "127.0.0.1";
    public static final int CLIENT_MANAGER_PORT = 50789;
    private final ServerSocket sc = new ServerSocket(CLIENT_MANAGER_PORT);
    private final Thread clientThread;
    private boolean shutdown = false;

    public ClientManager() throws IOException {
        ClientManagerWorker clientManagerWorker = new ClientManagerWorker(sc);
        clientThread = new Thread(clientManagerWorker);
        clientThread.start();
    }

    public int getPort() {
        return sc.getLocalPort();
    }

    public void stop() throws IOException, InterruptedException {
        shutdown = true;
        sc.close();
        clientThread.interrupt();
        clientThread.join();
    }

    private class ClientManagerWorker implements Runnable {
        private final ServerSocket sc;

        public ClientManagerWorker(ServerSocket sc) {
            this.sc = sc;
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try (Socket socket = sc.accept()) {
                    try (DataInputStream dis = new DataInputStream(socket.getInputStream());
                         DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                        ClientInitMessage clientInit = ClientInitMessage.parseDelimitedFrom(dis);
                        System.out.println(clientInit.getStrategy());
                        Client client = ClientBuilder.buildClient(clientInit.getStrategy());
                        Thread t = new Thread(client);
                        t.start();
                        t.interrupt();
                        t.join();
                        client.collectStatistic().writeDelimitedTo(dos);
                        dos.flush();
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
