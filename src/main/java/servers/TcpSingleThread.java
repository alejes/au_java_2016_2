package servers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TcpSingleThread extends Server {
    private ServerSocket serverSocket = new ServerSocket(0);
    private boolean shutdown = false;

    public TcpSingleThread() throws IOException {
        super();
    }

    @Override
    protected void stopServer() throws InterruptedException, IOException {
        shutdown = true;
        serverSocket.close();
        serverSocket = null;
    }

    @Override
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted() && !shutdown) {
                Socket socket = serverSocket.accept();
                try (DataInputStream dis = new DataInputStream(socket.getInputStream());
                     DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                    int arrayLength = dis.readInt();
                    ++totalClientsQueries;
                    int[] array = new int[arrayLength];


                    long startAllTime = System.nanoTime();
                    for (int i = 0; i < arrayLength; ++i) {
                        array[i] = dis.readInt();
                    }
                    long startSort = System.nanoTime();
                    /*
                    * we can export sort to external function but we dont want measure time for function calls
                     */
                    for (int i = 0; i < arrayLength; ++i) {
                        for (int j = 0; j < arrayLength; ++j) {
                            if (array[i] > array[j]) {
                                int temp = array[i];
                                array[i] = array[j];
                                array[j] = temp;
                            }
                        }
                    }
                    long timeSort = System.nanoTime() - startSort;
                    for (int val : array) {
                        dos.writeInt(val);
                    }
                    dos.flush();
                    long timeThisQuery = System.nanoTime() - startAllTime;
                    totalClientProcessingTime += timeSort;
                    totalQueryProcessingTime += timeThisQuery;
                } catch (IOException e) {
                    Logger log = Logger.getLogger(Server.class.getName());
                    log.log(Level.SEVERE, e.getMessage(), e);
                }

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
    }
}
