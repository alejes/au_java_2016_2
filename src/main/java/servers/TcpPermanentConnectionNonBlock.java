package servers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
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
        channel.socket().close();
        channel = null;
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        for (ServerWorker sw : workers) {
            totalClientProcessingTime += sw.localTotalClientProcessingTime;
            totalQueryProcessingTime += sw.localTotalQueryProcessingTime;
            totalClientsQueries += sw.localTotalClientsQueries;
        }
        //System.out.println("server stops");
    }

    @Override
    public void run() {
        try {
            Selector selector = Selector.open();

            SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_ACCEPT);
            LinkedList<Future<?>> waitedTasks = new LinkedList<>();

            while (!Thread.interrupted() && !shutdown) {

                /*Iterator<Future<ServerWorker>> iterator = waitedTasks.iterator();

                while (iterator.hasNext()) {
                    Future<ServerWorker> next = iterator.next();
                    if (next.isDone()) {
                        System.out.println("new task evaluated");
                        ServerWorker sw = next.get();
                        sw.key.interestOps(sw.key.interestOps() & (~SelectionKey.OP_WRITE));
                        *//*SocketChannel client = (SocketChannel) sw.key.channel();
                        client.register(selector, SelectionKey.OP_WRITE);*//*
                        iterator.remove();
                    }
                }*/

                int readyChannels = selector.select(1);
                if (readyChannels == 0) continue;
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        System.out.println("new accept");
                        SocketChannel client = channel.accept();
                        client.configureBlocking(false);
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_READ);
                        ServerWorker sw = new ServerWorker(key2);
                        workers.add(sw);
                        key2.attach(sw);
                    } else if (key.isReadable()) {
                        //System.out.println("new read");
                        SocketChannel client = (SocketChannel) key.channel();
                        ServerWorker worker = (ServerWorker) key.attachment();
                        if (worker.read(client)) {
                            System.out.println("read is finished");
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                            Future<?> taskResult = executorService.submit(worker);
                            waitedTasks.add(taskResult);
                        }

                    } else if (key.isWritable()) {
                        System.out.println("new write");
                        SocketChannel client = (SocketChannel) key.channel();
                        ServerWorker worker = (ServerWorker) key.attachment();
                        if (worker.write(client)) {
                            System.out.println("write is finished");
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
        } finally {
            //System.out.println("server evaluator stop");
        }
    }

    private static class ServerWorker implements Runnable {
        private final SelectionKey key;
        public long localTotalClientProcessingTime = 0;
        public long localTotalQueryProcessingTime = 0;
        public int localTotalClientsQueries = 0;
        private ByteBuffer source = ByteBuffer.allocate(2 * Integer.BYTES);
        private boolean isInitialized = false;
        private int retryCount = 0;
        private int arrayLength = 0;
        private int[] array;

        public ServerWorker(SelectionKey key) {
            /*if (!source.hasRemaining()) {
                source.rewind();
            }*/
            this.key = key;
        }

        public boolean registerTryAndCheckContinue() {
            return retryCount > 0;
        }

        public boolean read(SocketChannel client) throws IOException {
            client.read(source);
            if (!isInitialized) {
                //System.out.println(source.position());
                if (!source.hasRemaining()) {
                    source.flip();
                    retryCount = source.getInt();
                    arrayLength = source.getInt();
                    System.out.println("retry=" + retryCount + ";arrayLength=" + arrayLength);
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
        public void run()  {
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
            System.out.println(source.position());
            return !source.hasRemaining();
        }
    }
}
