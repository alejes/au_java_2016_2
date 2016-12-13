package managers;

import java.io.IOException;

public class ServerManagerCmd {
    public static void main(String[] args) throws IOException {
        ServerManager sm = new ServerManager();
        System.out.println("Start server manager on port " + sm.getPort());
    }
}
