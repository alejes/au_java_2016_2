package servers;

import utils.ArrayAlgorithms;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UdpFixedPool extends Server {
    private final ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private DatagramSocket serverSocket = new DatagramSocket(0);
    private boolean shutdown = false;
    private final List<ServerWorker> workers = new ArrayList<>();

    public UdpFixedPool() throws IOException {
        super();
    }

    @Override
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    protected void stopServer() throws InterruptedException, IOException {
        shutdown = true;
        serverSocket.close();
        serverSocket = null;
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        for (ServerWorker sw : workers) {
            totalClientProcessingTime += sw.localTotalClientProcessingTime;
            totalQueryProcessingTime += sw.localTotalQueryProcessingTime;
        }
        totalClientsQueries = workers.size();
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
                workers.add(sw);
                executorService.submit(sw);
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

    private class ServerWorker implements Runnable {
        private final DatagramPacket packet;
        private final DatagramSocket serverSocket;
        public long localTotalClientProcessingTime = 0;
        public long localTotalQueryProcessingTime = 0;

        private ServerWorker(DatagramSocket serverSocket, DatagramPacket packet) {
            this.packet = packet;
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[packet.getLength()];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(packet.getLength());
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData()));
                 DataOutputStream dos = new DataOutputStream(outputStream)) {
                int arrayLength = dis.readInt();
                int[] array = new int[arrayLength];
                long startAllTime = System.nanoTime();
                for (int i = 0; i < arrayLength; ++i) {
                    array[i] = dis.readInt();
                }
                long startSort = System.nanoTime();
                ArrayAlgorithms.squareSort(array);
                long timeSort = System.nanoTime() - startSort;
                for (int val : array) {
                    dos.writeInt(val);
                }
                dos.flush();
                DatagramPacket result = new DatagramPacket(buffer, dos.size(), packet.getAddress(), packet.getPort());
                serverSocket.send(result);
                long timeThisQuery = System.nanoTime() - startAllTime;
                localTotalClientProcessingTime += timeSort;
                localTotalQueryProcessingTime += timeThisQuery;

            } catch (IOException e) {
                Logger log = Logger.getLogger(Server.class.getName());
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
