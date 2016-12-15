package servers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TcpPermanentConnectionNewThread extends Server {
    private boolean shutdown = false;
    private List<Thread> threads = new ArrayList<>();

    public TcpPermanentConnectionNewThread() throws IOException {
        super();
    }

    @Override
    protected void stopServer() throws InterruptedException, IOException {
        shutdown = true;
        serverSocket.close();
        serverSocket = null;
        for (Thread t : threads) {
            t.interrupt();
            t.join();
        }
        System.out.println("server stops");
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted() && !shutdown) {
                Socket socket = serverSocket.accept();
                ServerWorker sw = new ServerWorker(socket);
                Thread t = new Thread(sw);
                threads.add(t);
                t.start();
            }
        } catch (SocketException e) {
            if (!shutdown) {
                Logger log = Logger.getLogger(Server.class.getName());
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        } catch (IOException e) {
            Logger log = Logger.getLogger(Server.class.getName());
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        finally {
            System.out.println("server evaluator stop");
        }
    }

    private static class ServerWorker implements Runnable {
        private final Socket socket;

        private ServerWorker(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Server worker starts");
            try (DataInputStream dis = new DataInputStream(socket.getInputStream());
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                int arrayLength = dis.readInt();
                int[] array = new int[arrayLength];

                for (int i = 0; i < arrayLength; ++i) {
                    array[i] = dis.readInt();
                }
                dos.flush();
                for (int i = 0; i < arrayLength; ++i) {
                    for (int j = 0; j < arrayLength; ++j) {
                        if (array[i] > array[j]) {
                            int temp = array[i];
                            array[i] = array[j];
                            array[j] = temp;
                        }
                    }
                }
                for (int val : array) {
                    dos.writeInt(val);
                }
            } catch (IOException e) {
                Logger log = Logger.getLogger(Server.class.getName());
                log.log(Level.SEVERE, e.getMessage(), e);
            }
            System.out.println("Server worker ends");
        }
    }
}
