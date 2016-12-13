package managers;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerManager {
    private final ServerSocket sc = new ServerSocket(0);

    public ServerManager() throws IOException {

    }

    int getPort(){
        return sc.getLocalPort();
    }
}
