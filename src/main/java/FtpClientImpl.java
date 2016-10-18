import exceptions.FTPException;
import ftp.FtpClient;
import models.requests.GetRequest;
import models.requests.ListRequest;
import models.responses.GetResponse;
import models.responses.ListResponse;
import models.utils.StreamFtpInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FtpClientImpl implements FtpClient {
    private final String serverHost = "127.0.0.1";
    private final byte[] byteBuffer = new byte[1024];
    private final int serverPort;
    private Socket socket = null;

    public FtpClientImpl() {
        this.serverPort = 8000;
    }

    public FtpClientImpl(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void connect() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverHost, serverPort), 5000);
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    @Override
    public void disconnect() {
        checkActiveConnection();
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    @Override
    public ListResponse executeList(ListRequest request) {
        checkActiveConnection();
        try {
            OutputStream os = socket.getOutputStream();
            os.write(request.toByteArray());
            os.flush();
            InputStream is = socket.getInputStream();
            int cnt = is.read(byteBuffer);
            if (cnt < 0) {
                return null;
            }
            String result = new String(byteBuffer).substring(0, cnt);

            return new ListResponse(result);
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    @Override
    public GetResponse executeGet(GetRequest request) {
        checkActiveConnection();
        try {
            StringBuilder result = new StringBuilder("");
            OutputStream os = socket.getOutputStream();
            os.write(request.toByteArray());
            os.flush();
            InputStream is = socket.getInputStream();
            StreamFtpInputStream ftpStream = new StreamFtpInputStream(is);

            while (ftpStream.available() > 0) {
                int cnt = ftpStream.read(byteBuffer);
                if (cnt < 0) {
                    break;
                }
                result.append(new String(byteBuffer).substring(0, cnt));
            }

            return new GetResponse(result.toString());
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    @Override
    public GetResponse executeGetLazy(GetRequest request) {
        checkActiveConnection();
        try {
            OutputStream os = socket.getOutputStream();
            os.write(request.toByteArray());
            os.flush();
            InputStream is = socket.getInputStream();
            return new GetResponse(new StreamFtpInputStream(is));
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    private void checkActiveConnection() {
        if ((socket == null) || (socket.isClosed())) {
            throw new FTPException("You are not connected to server");
        }
    }
}
