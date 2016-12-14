package servers;

import managers.ServerManager;;
import proto.ServerResponseStatMessageOuterClass.ServerResponseStatMessage;

import java.io.IOException;
import java.net.ServerSocket;

import static proto.ServerDataOuterClass.ServerData;

public abstract class Server implements Runnable {
    protected ServerSocket socket = new ServerSocket(0);

    protected Server() throws IOException {
    }

    public ServerData getServerData() {
        return ServerData.newBuilder().setServerIp(ServerManager.SERVER_MANAGER_HOST)
                .setServerPort(socket.getLocalPort())
                .build();
    }

    public abstract ServerResponseStatMessage stopAndCollectStatistic();

}
