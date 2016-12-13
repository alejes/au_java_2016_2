package managers;

import java.io.IOException;

public class ClientManagerCmd {
    public static void main(String[] args) throws IOException {
        ClientManager cm = new ClientManager();
        System.out.println("Start client manager on port " + cm.getPort());
    }
}
