package servers;

import utils.ArrayAlgorithms;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UdpNewThread extends UdpServer {

    private final List<Thread> threads = new ArrayList<>();
    private final List<ServerWorker> workers = new ArrayList<>();

    public UdpNewThread() throws IOException {
        super();
        serverSocket = new DatagramSocket(0);
    }

    @Override
    protected void stopServer() throws InterruptedException, IOException {
        super.stopServer();
        for (Thread t : threads) {
            t.interrupt();
            t.join();
        }
        for (ServerWorker sw : workers) {
            totalClientProcessingTime += sw.localTotalClientProcessingTime;
            totalQueryProcessingTime += sw.localTotalQueryProcessingTime;
        }
        totalClientsQueries = workers.size();
        workers.clear();
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted() && !shutdown) {
                DatagramPacket packet =
                        new DatagramPacket(
                                new byte[serverSocket.getReceiveBufferSize()],
                                serverSocket.getReceiveBufferSize());
                serverSocket.receive(packet);
                ServerWorker sw = new ServerWorker(serverSocket, packet);
                Thread t = new Thread(sw);
                threads.add(t);
                workers.add(sw);
                t.start();
            }
        } catch (SocketException e) {
            if (!shutdown) {
                Logger log = Logger.getLogger(Server.class.getName());
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        } catch (IOException e) {
            Logger log = Logger.getLogger(Server.class.getName());
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
