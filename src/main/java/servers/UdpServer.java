package servers;


import utils.ArrayAlgorithms;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UdpServer extends Server {
    protected DatagramSocket serverSocket = new DatagramSocket( new InetSocketAddress("0.0.0.0", 0));

    protected UdpServer() throws SocketException {
        serverSocket.setSendBufferSize(1 << 16);
    }

    @Override
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    protected void stopServer() throws InterruptedException, IOException {
        shutdown = true;
        serverSocket.close();
        serverSocket = null;
    }

    protected class ServerWorker implements Runnable {
        private final DatagramPacket packet;
        private final DatagramSocket serverSocket;
        public long localTotalClientProcessingTime = 0;
        public long localTotalQueryProcessingTime = 0;

        protected ServerWorker(DatagramSocket serverSocket, DatagramPacket packet) {
            this.packet = packet;
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[packet.getLength()];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(packet.getLength());
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData()));
                 DataOutputStream dos = new DataOutputStream(outputStream)) {
                int arrayLength = dis.readInt();
                int[] array = new int[arrayLength];
                long startAllTime = System.nanoTime();
                for (int i = 0; i < arrayLength; ++i) {
                    array[i] = dis.readInt();
                }
                long startSort = System.nanoTime();
                ArrayAlgorithms.squareSort(array);
                long timeSort = System.nanoTime() - startSort;
                for (int val : array) {
                    dos.writeInt(val);
                }
                dos.flush();
                DatagramPacket result = new DatagramPacket(buffer, dos.size(), packet.getAddress(), packet.getPort());
                serverSocket.send(result);
                long timeThisQuery = System.nanoTime() - startAllTime;
                localTotalClientProcessingTime += timeSort;
                localTotalQueryProcessingTime += timeThisQuery;

            } catch (IOException e) {
                Logger log = Logger.getLogger(Server.class.getName());
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
