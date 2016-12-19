package servers;

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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TcpPermanentConnectionCache extends Server {
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ServerSocket serverSocket = new ServerSocket(0);
    private boolean shutdown = false;
    private List<Future> resultFutures = new ArrayList<>();
    private List<ServerWorker> workers = new ArrayList<>();

    public TcpPermanentConnectionCache() throws IOException {
        super();
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
            totalClientsQueries += sw.localTotalClientsQueries;
        }
        //System.out.println("server stops");
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
        } finally {
            //System.out.println("server evaluator stop");
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
            //System.out.println("Server worker starts");
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
                    /*
                    * we can export sort to external function but we dont want measure time for function calls
                     */
                    for (int i = 0; i < arrayLength; ++i) {
                        for (int j = 0; j < arrayLength; ++j) {
                            if (array[i] > array[j]) {
                                int temp = array[i];
                                array[i] = array[j];
                                array[j] = temp;
                            }
                        }
                    }
                    long timeSort = System.nanoTime() - startSort;
                    for (int val : array) {
                        dos.writeInt(val);
                    }
                    dos.flush();
                    long timeThisQuery = System.nanoTime() - startAllTime;
                    //System.out.println(allSortTime);
                    localTotalClientProcessingTime += timeSort;
                    localTotalQueryProcessingTime += timeThisQuery;
                }
            } catch (IOException e) {
                Logger log = Logger.getLogger(Server.class.getName());
                log.log(Level.SEVERE, e.getMessage(), e);
            }
            //System.out.println("Server worker ends");
        }
    }
}
