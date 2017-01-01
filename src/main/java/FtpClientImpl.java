import exceptions.FTPException;
import ftp.FtpClient;
import models.FtpFile;
import models.requests.GetRequest;
import models.requests.ListRequest;
import models.responses.GetResponse;
import models.responses.ListResponse;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FtpClientImpl implements FtpClient {
    private final String serverHost = "127.0.0.1";
    private final int serverPort;
    private Socket socket = null;

    public FtpClientImpl() {
        this(8000);
    }


    public FtpClientImpl(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void connect() throws FTPException {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverHost, serverPort), 5000);
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    @Override
    public void disconnect() throws FTPException {
        checkActiveConnection();
        try {
            socket.close();
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    @Override
    public ListResponse executeList(ListRequest request) throws FTPException {
        checkActiveConnection();
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            request.dump(dos);
            dos.flush();
            int itemsCount = dis.readInt();
            List<FtpFile> items = new ArrayList<>();
            for (int i = 0; i < itemsCount; ++i) {
                String name = dis.readUTF();
                Boolean isDir = dis.readBoolean();
                items.add(new FtpFile(isDir, name.trim()));
            }

            return new ListResponse(items);
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    @Override
    public GetResponse executeGet(GetRequest request) throws FTPException {
        checkActiveConnection();
        try {
            OutputStream os = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            request.dump(dos);
            os.flush();
            InputStream is = socket.getInputStream();
            DataInputStream ftpStream = new DataInputStream(is);
            return new GetResponse(ftpStream);
        } catch (IOException e) {
            throw new FTPException("IOException: ", e);
        }
    }

    private void checkActiveConnection() throws FTPException {
        if ((socket == null) || socket.isClosed()) {
            throw new FTPException("You are not connected to server");
        }
    }
}
