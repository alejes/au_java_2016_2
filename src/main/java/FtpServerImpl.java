import exceptions.FTPException;
import ftp.Ftp;
import ftp.FtpServer;

import java.io.IOException;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

public class FtpServerImpl extends Ftp implements FtpServer {
    private String host = "127.0.0.1";
    private int port = 8000;

    @Override
    public void serverStart() {
        try {
            // Создаём Selector
            Selector selector = SelectorProvider.provider().openSelector();

            ServerSocketChannel server = ServerSocketChannel.open();
            // nonblocking I/O
            server.configureBlocking(false);
            // host-port 8000
            server.socket().bind(new java.net.InetSocketAddress(host, port));
            server.register(selector, server.validOps());


            while (selector.select() > -1) {
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
                                // Принимаем соединение
                                accept(key);
                            } else if (key.isConnectable()) {
                                // Устанавливаем соединение
                                connect(key);
                            } else if (key.isReadable()) {
                                // Читаем данные
                                read(key);
                            } else if (key.isWritable()) {
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

    private void close(SelectionKey key) {
        
    }

    private void read(SelectionKey key) {
    }

    private void write(SelectionKey key) {
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
}
