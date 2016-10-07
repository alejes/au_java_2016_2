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
    private String serverHost = "127.0.0.1";
    private int serverPort = 8000;
    private Socket socket = null;
    private byte[] byteBuffer = new byte[1024];

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
        try {
            OutputStream os = socket.getOutputStream();
            os.write(request.toByteArray());
            os.flush();
            InputStream is = socket.getInputStream();
            int cnt = is.read(byteBuffer);
            String result = new String(byteBuffer).substring(0, cnt);
            System.out.println("Receive: " + result);
            return null;
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    @Override
    public GetResponse executeGet(GetRequest request) {
        try {
            OutputStream os = socket.getOutputStream();
            os.write(request.toByteArray());
            os.flush();
            InputStream is = socket.getInputStream();
            int cnt = is.read(byteBuffer);
            if (cnt < 0){
                return null;
            }
            System.out.println("size=" + cnt);
            for (int i = 0; i < cnt; ++i){
                int val = ((int)byteBuffer[i]);
                System.out.print(val);
            }
            String result = new String(byteBuffer).substring(0, cnt);
            System.out.println("Receive: " + result);
            return null;
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }
}
