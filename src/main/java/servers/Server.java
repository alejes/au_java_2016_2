package servers;

import managers.ServerManager;;
import proto.ServerResponseStatMessageOuterClass.ServerResponseStatMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static proto.ServerDataOuterClass.ServerData;

public abstract class Server implements Runnable {
    protected long totalQueryProcessingTime = 0;
    protected int totalClientsQueries = 0;
    protected long totalClientProcessingTime = 0;

    protected Server() throws IOException {
    }

    public abstract int getPort() throws IOException;

    public ServerData getServerData() throws IOException {
        //System.out.println("Server port=" + serverSocket.getLocalPort());
        return ServerData.newBuilder().setServerIp(ServerManager.SERVER_MANAGER_HOST)
                .setServerPort(getPort())
                .build();
    }

    protected abstract void stopServer() throws InterruptedException, IOException;

    public ServerResponseStatMessage stopAndCollectStatistic() throws InterruptedException, IOException {
        stopServer();
        return ServerResponseStatMessage.newBuilder()
                .setQueryProcessingTime(((totalClientsQueries) == 0) ? 0 : 1.0 * totalQueryProcessingTime / (1_000_000 * totalClientsQueries))
                .setClientProcessingTime(((totalClientsQueries) == 0) ? 0 : 1.0 * totalClientProcessingTime / (1_000_000 * totalClientsQueries))
                .build();
    }

}
