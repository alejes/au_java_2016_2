import exceptions.FTPException;
import ftp.FtpClient;
import models.requests.GetRequest;
import models.requests.ListRequest;
import models.responses.GetResponse;
import models.responses.ListResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FtpClientImpl implements FtpClient {
    private final String serverHost = "127.0.0.1";
    private final int serverPort = 8000;
    private final byte[] byteBuffer = new byte[1024];
    private Socket socket = null;

    @Override
    public void connect() {
        try {
            socket = new Socket(serverHost, serverPort);
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
            OutputStream os = socket.getOutputStream();
            os.write(request.toByteArray());
            os.flush();
            InputStream is = socket.getInputStream();
            int cnt = is.read(byteBuffer);
            if (cnt < 0) {
                return null;
            }
            String result = new String(byteBuffer).substring(0, cnt);
            return new GetResponse(result);
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
