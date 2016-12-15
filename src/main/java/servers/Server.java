package servers;

import managers.ServerManager;;
import proto.ServerResponseStatMessageOuterClass.ServerResponseStatMessage;

import java.io.IOException;
import java.net.ServerSocket;

import static proto.ServerDataOuterClass.ServerData;

public abstract class Server implements Runnable {
    protected ServerSocket serverSocket = new ServerSocket(0);
    protected int queryProcessingTime;
    protected int clientProcessingTime;

    protected Server() throws IOException {
    }

    public ServerData getServerData() {
        System.out.println("Server port=" + serverSocket.getLocalPort());
        return ServerData.newBuilder().setServerIp(ServerManager.SERVER_MANAGER_HOST)
                .setServerPort(serverSocket.getLocalPort())
                .build();
    }

    protected abstract void stopServer() throws InterruptedException;

    public ServerResponseStatMessage stopAndCollectStatistic() throws InterruptedException {
        stopServer();
        return ServerResponseStatMessage.newBuilder()
                .setQueryProcessingTime(queryProcessingTime)
                .setClientProcessingTime(clientProcessingTime)
                .build();
    }

}
