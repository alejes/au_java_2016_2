import exceptions.TorrentException;
import models.requests.Request;
import models.requests.server.UpdateRequest;
import models.response.Response;
import models.response.server.UpdateResponse;
import models.torrent.TorrentClient;
import models.torrent.TorrentClientState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class TorrentClientImpl implements TorrentClient {
    private final String serverHost;
    private final int serverPort;
    private Thread updateThread;
    private TorrentClientState tcs;

    public TorrentClientImpl(String serverHost, int serverPort) throws IOException {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        ServerSocket server = new ServerSocket(0);
        tcs = new TorrentClientState(server);
    }

    private void sendRequest(Request request, Response response) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(serverHost, serverPort), 5000);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            request.writeToDataOutputStream(dos);
            response.readFromDataInputStream(dis);
        } catch (IOException e) {
            throw new TorrentException("IOException: ", e);
        }
    }

    @Override
    public void update() {
        updateThread = new Thread(() -> {
            while(!Thread.interrupted()) {
                Request updateRequest = new UpdateRequest(tcs);
                Response updateResponse = new UpdateResponse();
                sendRequest(updateRequest, updateResponse);
                try {
                    Thread.sleep(4500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updateThread.start();
    }
}
