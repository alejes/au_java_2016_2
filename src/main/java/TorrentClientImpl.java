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
    private Thread updateThread;
    private TorrentClientState tcs;

    public TorrentClientImpl(String serverHost) throws IOException {
        this.serverHost = serverHost;
        ServerSocket server = new ServerSocket(0);
        tcs = new TorrentClientState(server);
        update();
    }

    private void sendRequest(Request request, Response response) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(serverHost, getServerPort()), 5000);
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
            while (!Thread.interrupted()) {
                Request updateRequest = new UpdateRequest(tcs);
                Response updateResponse = new UpdateResponse();
                try {
                    sendRequest(updateRequest, updateResponse);
                } catch (TorrentException e) {
                    System.out.println("TorrentException: " + e.getMessage());
                }
                System.out.println("Information updated");
                try {
                    Thread.sleep((long) (4.5 * 60 * 1000));
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updateThread.start();
    }
}
