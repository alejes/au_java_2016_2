package servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UdpFixedPool extends UdpServer {
    private final ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final List<ServerWorker> workers = new ArrayList<>();

    public UdpFixedPool() throws IOException {
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
}
