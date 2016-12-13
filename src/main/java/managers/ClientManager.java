package managers;


import java.io.IOException;
import java.net.ServerSocket;

public class ClientManager {
    private final ServerSocket sc = new ServerSocket(0);

    public ClientManager() throws IOException {

    }

    int getPort(){
        return sc.getLocalPort();
    }


}
