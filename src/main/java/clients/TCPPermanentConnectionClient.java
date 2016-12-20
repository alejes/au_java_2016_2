package clients;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TCPPermanentConnectionClient extends Client {
    public TCPPermanentConnectionClient() {
        super();
    }

    @Override
    public void run() {
        long start, end;
        start = System.nanoTime();
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
                        receivedArray[i] = dis.readInt();
                    }
                    if ((requestId < initMessage.getX() - 1) && (initMessage.getDelta() > 0)) {
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

        end = System.nanoTime();
        averageClientTime = 1.0 * (end - start) / 1_000_000;
    }
}
