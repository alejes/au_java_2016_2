package servers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TcpAsync extends Server {
    private boolean shutdown = false;
    private List<ServerWorker> workers = new ArrayList<>();
    private AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open().bind(null);

    public TcpAsync() throws IOException {
        super();
    }

    @Override
    public int getPort() throws IOException {
        return ((InetSocketAddress) channel.getLocalAddress()).getPort();
    }

    @Override
    protected void stopServer() throws InterruptedException, IOException {
        shutdown = true;
        channel.close();
        channel = null;
        for (ServerWorker sw : workers) {
            totalClientProcessingTime += sw.localTotalClientProcessingTime;
            totalQueryProcessingTime += sw.localTotalQueryProcessingTime;
        }
        totalClientsQueries = workers.size();
        workers.clear();
    }

    @Override
    public void run() {
        channel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            public void completed(AsynchronousSocketChannel clientChannel, Void att) {
                channel.accept(null, this);
                ServerWorker serverWorker = new ServerWorker();
                workers.add(serverWorker);
                serverWorker.registerReadHeader(clientChannel);
            }

            public void failed(Throwable e, Void att) {
                if (!shutdown) {
                    Logger log = Logger.getLogger(Server.class.getName());
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
                Thread.currentThread().interrupt();
            }
        });
    }

    private static class ServerWorker {
        public long localTotalClientProcessingTime = 0;
        public long localTotalQueryProcessingTime = 0;
        private ByteBuffer source = ByteBuffer.allocate(Integer.BYTES);
        private int arrayLength = 0;
        private int[] array;
        private long startAllTime;

        public void registerReadHeader(AsynchronousSocketChannel clientChannel) {
            startAllTime = System.nanoTime();
            clientChannel.read(source, this, new CompletionHandler<Integer, ServerWorker>() {
                @Override
                public void completed(Integer result, ServerWorker attachment) {
                    if (source.hasRemaining()) {
                        clientChannel.read(source, attachment, this);
                    } else {
                        source.flip();
                        arrayLength = source.getInt();
                        array = new int[arrayLength];
                        source = ByteBuffer.allocate(arrayLength * Integer.BYTES);
                        registerRead(clientChannel);
                    }
                }

                @Override
                public void failed(Throwable e, ServerWorker attachment) {
                    Logger log = Logger.getLogger(Server.class.getName());
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
            });
        }

        private void registerRead(AsynchronousSocketChannel clientChannel) {
            clientChannel.read(source, this, new CompletionHandler<Integer, ServerWorker>() {
                @Override
                public void completed(Integer result, ServerWorker attachment) {
                    if (source.hasRemaining()) {
                        clientChannel.read(source, attachment, this);
                    } else {
                        source.flip();
                        for (int i = 0; i < arrayLength; ++i) {
                            array[i] = source.getInt();
                        }
                        long startSort = System.nanoTime();
                        for (int i = 0; i < arrayLength; ++i) {
                            for (int j = 0; j < arrayLength; ++j) {
                                if (array[i] > array[j]) {
                                    int temp = array[i];
                                    array[i] = array[j];
                                    array[j] = temp;
                                }
                            }
                        }
                        localTotalClientProcessingTime = System.nanoTime() - startSort;

                        source.clear();
                        for (int i = 0; i < arrayLength; ++i) {
                            source.putInt(array[i]);
                        }
                        source.flip();
                        registerWrite(clientChannel);
                    }
                }

                @Override
                public void failed(Throwable e, ServerWorker attachment) {
                    Logger log = Logger.getLogger(Server.class.getName());
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
            });
        }

        private void registerWrite(AsynchronousSocketChannel clientChannel) {
            clientChannel.write(source, this, new CompletionHandler<Integer, ServerWorker>() {
                @Override
                public void completed(Integer result, ServerWorker attachment) {
                    if (source.hasRemaining()) {
                        clientChannel.write(source, attachment, this);
                    } else {
                        localTotalQueryProcessingTime = System.nanoTime() - startAllTime;
                    }
                }

                @Override
                public void failed(Throwable e, ServerWorker attachment) {
                    Logger log = Logger.getLogger(Server.class.getName());
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
            });
        }

    }
}
