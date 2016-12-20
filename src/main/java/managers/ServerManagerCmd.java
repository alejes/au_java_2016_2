package managers;

import java.io.IOException;

public class ServerManagerCmd {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerManager sm = new ServerManager();
        System.out.println("Start server manager on port " + sm.getPort());
        System.out.println("Enter any key to interrupt...");
        System.in.read();
        sm.stop();
    }
}
