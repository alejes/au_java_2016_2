import exceptions.FTPException;
import ftp.Ftp;
import ftp.FtpServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

public class FtpServerImpl extends Ftp implements FtpServer {
    private static int bufferSize = 4096;
    private String serverHost = "127.0.0.1";
    private int serverPort = 8000;

    @Override
    public void serverStart() {
        try {
            // Создаём Selector
            Selector selector = SelectorProvider.provider().openSelector();

            ServerSocketChannel server = ServerSocketChannel.open();
            // nonblocking I/O
            server.configureBlocking(false);
            server.socket().bind(new java.net.InetSocketAddress(serverHost, serverPort));
            server.register(selector, server.validOps());


            while (selector.select() > -1) {
                System.out.println("Iterators get");
                // Получаем ключи на которых произошли события в момент
                // последней выборки
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isValid()) {
                        // Обработка всех возможнных событий ключа
                        try {
                            if (key.isAcceptable()) {
                                System.out.println("new accept");
                                // Принимаем соединение
                                accept(key);
                            } else if (key.isConnectable()) {
                                System.out.println("new connect");
                                // Устанавливаем соединение
                                connect(key);
                            } else if (key.isReadable()) {
                                System.out.println("new read");
                                // Читаем данные
                                read(key);
                            } else if (key.isWritable()) {
                                System.out.println("new write");
                                // Пишем данные
                                write(key);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            close(key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    private void close(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
    }

    private void read(SelectionKey key) {
        try {
            SocketChannel channel = ((SocketChannel) key.channel());
            Attachment attachment = ((Attachment) key.attachment());
            if (attachment == null) {
                // Лениво инициализируем буферы
                key.attach(attachment = new Attachment());
                attachment.in = ByteBuffer.allocate(bufferSize);
            }
            System.out.println("Start read section");
            int bytesRead;
            if ((bytesRead = channel.read(attachment.in)) < 1) {
                close(key);
            } else {
                System.out.println("Read " + bytesRead);
                attachment.in.flip();
                while (attachment.in.hasRemaining()) {
                    System.out.print((char) attachment.in.get());
                }
                attachment.in.clear();
                // ну а если мы проксируем, то добавляем ко второму концу интерес
                // записать
                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                // а у первого убираем интерес прочитать, т.к пока не записали
                // текущие данные, читать ничего не будем
                key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
                // готовим буфер для записи
                attachment.in.flip();
                if (attachment.out == null) {
                    attachment.out = ByteBuffer.allocate(1024);
                }
                attachment.out.put("hello".getBytes());
            }
            /*
            if (channel.read(attachment.in) < 1) {
                // -1 - разрыв 0 - нету места в буфере, такое может быть только если
                // заголовок превысил размер буфера
                close(key);
            } else if (attachment.peer == null) {
                // если нету второго конца :) стало быть мы читаем заголовок
                //readHeader(key, attachment);
            } else {


            }*/
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    private void write(SelectionKey key) {
        try {
            // Закрывать сокет надо только записав все данные
            SocketChannel channel = ((SocketChannel) key.channel());
            Attachment attachment = ((Attachment) key.attachment());
            System.out.println("Buffer " + attachment.out.toString());
            if (channel.write(attachment.out) == -1) {
                close(key);
            } else if (attachment.out.remaining() == 0) {
                // если всё записано, чистим буфер
                attachment.out.clear();
                // Добавялем ко второму концу интерес на чтение
                key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                // А у своего убираем интерес на запись
                key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    private void connect(SelectionKey key) {
    }

    private void accept(SelectionKey key) throws IOException, ClosedChannelException {
        // Приняли
        SocketChannel newChannel = ((ServerSocketChannel) key.channel()).accept();
        // Неблокирующий
        newChannel.configureBlocking(false);
        // Регистрируем в селекторе
        newChannel.register(key.selector(), SelectionKey.OP_READ);
    }

    @Override
    public void serverStop() {

    }

    static class Attachment {
        /**
         * Буфер для чтения, в момент проксирования становится буфером для
         * записи для ключа хранимого в peer
         * <p>
         * ВАЖНО: При парсинге Socks4 заголовком мы предполагаем что размер
         * буфера, больше чем размер нормального заголовка, у браузера Mozilla
         * Firefox, размер заголовка равен 12 байт 1 версия + 1 команда + 2 порт +
         * 4 ip + 3 id (MOZ) + 1 \0
         */

        ByteBuffer in;
        /**
         * Буфер для записи, в момент проксирования равен буферу для чтения для
         * ключа хранимого в peer
         */
        ByteBuffer out;

    }
}
