package clients;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UdpNewConnectionClient extends Client {
    public UdpNewConnectionClient() throws IOException {
        super();
    }

    @Override
    public void run() {
        byte[] buffer = new byte[Integer.BYTES * array.length];
        long start, end;
        start = System.nanoTime();
        for (int requestId = 0; requestId < initMessage.getX(); ) {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(50);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(buffer.length + Integer.BYTES);
                try (DataOutputStream dos = new DataOutputStream(outputStream)) {
                    dos.writeInt(array.length);
                    for (int val : array) {
                        dos.writeInt(val);
                    }
                    dos.flush();
                    DatagramPacket packet =
                            new DatagramPacket(outputStream.toByteArray(),
                                    outputStream.size(),
                                    InetAddress.getByName(initMessage.getServer().getServerIp()),
                                    initMessage.getServer().getServerPort());
                    socket.send(packet);

                    DatagramPacket result = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(result);
                    } catch (SocketTimeoutException e) {
                        continue;
                    }

                    try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData()))) {
                        for (int i = 0; i < array.length; ++i) {
                            receivedArray[i] = dis.readInt();
                        }
                    }
                    ++requestId;
                    if ((requestId < initMessage.getX() - 1) && (initMessage.getDelta() > 0)) {
                        Thread.sleep(initMessage.getDelta());
                    }

                } catch (InterruptedException e) {
                    Logger log = Logger.getLogger(Client.class.getName());
                    log.log(Level.SEVERE, e.getMessage(), e);
                }

            } catch (IOException e) {
                Logger log = Logger.getLogger(Client.class.getName());
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        end = System.nanoTime();
        averageClientTime = 1.0 * (end - start) / 1_000_000;
    }
}
