package servers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TcpPermanentConnectionNonBlock extends Server {
    private final ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private boolean shutdown = false;
    private List<ServerWorker> workers = new ArrayList<>();
    private ServerSocketChannel channel;


    public TcpPermanentConnectionNonBlock() throws IOException {
        super();
        channel = ServerSocketChannel.open();
        channel.socket().bind(new InetSocketAddress(0));
        channel.configureBlocking(false);
    }

    @Override
    public int getPort() {
        return channel.socket().getLocalPort();
    }

    @Override
    protected void stopServer() throws InterruptedException, IOException {
        shutdown = true;
        channel.close();
        channel = null;
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        for (ServerWorker sw : workers) {
            totalClientProcessingTime += sw.localTotalClientProcessingTime;
            totalQueryProcessingTime += sw.localTotalQueryProcessingTime;
            totalClientsQueries += sw.localTotalClientsQueries;
        }
    }

    @Override
    public void run() {
        try {
            Selector selector = Selector.open();

            channel.register(selector, SelectionKey.OP_ACCEPT);

            while (!Thread.interrupted() && !shutdown) {
                int readyChannels = selector.select(1);
                if (readyChannels == 0) continue;
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        SocketChannel client = channel.accept();
                        client.configureBlocking(false);
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_READ);
                        ServerWorker sw = new ServerWorker(key2);
                        workers.add(sw);
                        key2.attach(sw);
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ServerWorker worker = (ServerWorker) key.attachment();
                        if (worker.read(client)) {
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                            worker.registerTask(executorService);
                        }
                    } else if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ServerWorker worker = (ServerWorker) key.attachment();
                        if (worker.write(client)) {
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                            if (worker.registerTryAndCheckContinue()) {
                                key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                            }
                        }
                    }
                    keyIterator.remove();
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

    private static class ServerWorker implements Runnable {
        private final SelectionKey key;
        private final long startAllTime;
        public long localTotalClientProcessingTime = 0;
        public long localTotalQueryProcessingTime = 0;
        public int localTotalClientsQueries = 0;
        private ByteBuffer source = ByteBuffer.allocate(2 * Integer.BYTES);
        private boolean isInitialized = false;
        private int retryCount = 0;
        private int arrayLength = 0;
        private int[] array;
        private long startSort = -1;

        public ServerWorker(SelectionKey key) {
            startAllTime = System.nanoTime();
            this.key = key;
        }

        public void registerTask(ExecutorService executorService) {
            startSort = System.nanoTime();
            executorService.submit(this);
        }

        public boolean registerTryAndCheckContinue() {
            startSort = System.nanoTime();
            return retryCount > 0;
        }

        public boolean read(SocketChannel client) throws IOException {
            client.read(source);
            if (!isInitialized) {
                if (!source.hasRemaining()) {
                    source.flip();
                    retryCount = source.getInt();
                    arrayLength = source.getInt();
                    localTotalClientsQueries += retryCount;
                    array = new int[arrayLength];
                    isInitialized = true;
                    source = ByteBuffer.allocate(Integer.BYTES * arrayLength);
                    client.read(source);
                }
            }
            return !source.hasRemaining();
        }

        @Override
        public void run() {
            source.flip();
            for (int i = 0; i < arrayLength; ++i) {
                array[i] = source.getInt();
            }
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
            localTotalClientProcessingTime += timeSort;

            source.clear();
            for (int i = 0; i < arrayLength; ++i) {
                source.putInt(array[i]);
            }
            source.flip();
            --retryCount;
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        }

        public boolean write(SocketChannel client) throws IOException {
            client.write(source);
            if (source.hasRemaining()) {
                return false;
            } else {
                long timeThisQuery = System.nanoTime() - startAllTime;
                localTotalQueryProcessingTime += timeThisQuery;
                return true;
            }
        }
    }
}
