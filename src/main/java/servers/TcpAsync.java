package servers;

import utils.ArrayAlgorithms;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TcpAsync extends TcpServer {
    private final List<ServerWorker> workers = new ArrayList<>();
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
        super.stopServer();
        try {
            for (ServerWorker sw : workers) {
                if (sw != null) {
                    totalClientProcessingTime += sw.localTotalClientProcessingTime;
                    totalQueryProcessingTime += sw.localTotalQueryProcessingTime;
                }
            }
        }catch (NullPointerException e){
            e.printStackTrace();;
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
                        ArrayAlgorithms.squareSort(array);
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
                        try {
                            clientChannel.close();
                        } catch (IOException e) {
                            Logger log = Logger.getLogger(Server.class.getName());
                            log.log(Level.SEVERE, e.getMessage(), e);
                        }
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
