package servers;

import utils.ArrayAlgorithms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TcpPermanentConnectionCache extends TcpServer {
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final List<ServerWorker> workers = new ArrayList<>();

    public TcpPermanentConnectionCache() throws IOException {
        super();
    }

    @Override
    protected void stopServer() throws InterruptedException, IOException {
        super.stopServer();
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        for (ServerWorker sw : workers) {
            totalClientProcessingTime += sw.localTotalClientProcessingTime;
            totalQueryProcessingTime += sw.localTotalQueryProcessingTime;
            totalClientsQueries += sw.localTotalClientsQueries;
        }
        workers.clear();
    }

    @Override
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted() && !shutdown) {
                Socket socket = serverSocket.accept();
                ServerWorker sw = new ServerWorker(socket);
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
        private final Socket socket;
        public long localTotalClientProcessingTime = 0;
        public long localTotalQueryProcessingTime = 0;
        public int localTotalClientsQueries = 0;

        private ServerWorker(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream dis = new DataInputStream(socket.getInputStream());
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                int retryCount = dis.readInt();
                int arrayLength = dis.readInt();
                localTotalClientsQueries += retryCount;
                int[] array = new int[arrayLength];

                for (int retryId = 0; retryId < retryCount; ++retryId) {
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
                    long timeThisQuery = System.nanoTime() - startAllTime;
                    localTotalClientProcessingTime += timeSort;
                    localTotalQueryProcessingTime += timeThisQuery;
                }
            } catch (IOException e) {
                Logger log = Logger.getLogger(Server.class.getName());
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
