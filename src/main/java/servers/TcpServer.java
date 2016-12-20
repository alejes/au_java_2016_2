package servers;

import java.io.IOException;
import java.net.ServerSocket;

public abstract class TcpServer extends Server {
    protected ServerSocket serverSocket = new ServerSocket(0);

    protected TcpServer() throws IOException {
    }

    @Override
    protected void stopServer() throws InterruptedException, IOException {
        shutdown = true;
        serverSocket.close();
        serverSocket = null;
    }
}
