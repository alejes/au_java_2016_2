package clients;

import org.omg.PortableServer.THREAD_POLICY_ID;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TCPPermanentConnectionClient extends Client {
    public TCPPermanentConnectionClient() throws IOException {
        super();
    }

    @Override
    public void run() {
        long start, end;
        System.out.println("try connect to " + initMessage.getServer().getServerIp() + ":" + initMessage.getServer().getServerPort());
        start = System.currentTimeMillis();
        try (Socket socket =
                     new Socket(initMessage.getServer().getServerIp(), initMessage.getServer().getServerPort())) {
            try (DataInputStream dis = new DataInputStream(socket.getInputStream());
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                dos.writeInt(initMessage.getX());
                dos.writeInt(array.length);
                for (int requestId = 0; requestId < initMessage.getX(); ++requestId) {
                    for (int val : array) {
                        dos.writeInt(val);
                    }
                    dos.flush();
                    for (int i = 0; i < array.length; ++i) {
                        array[i] = dis.readInt();
                    }
                    if (requestId < initMessage.getX() - 1) {
                        Thread.sleep(initMessage.getDelta());
                    }
                }
            } catch (InterruptedException e) {
                Logger log = Logger.getLogger(Client.class.getName());
                log.log(Level.SEVERE, e.getMessage(), e);
            }

        } catch (IOException e) {
            Logger log = Logger.getLogger(Client.class.getName());
            log.log(Level.SEVERE, e.getMessage(), e);
        }

        end = System.currentTimeMillis();
        System.out.println("client end");
        averageClientTime = end - start;
    }
}
