import exceptions.TorrentException;
import models.TorrentFile;
import models.TorrentPeer;
import models.commands.Command;
import models.commands.client.ClientCommandBuilder;
import models.requests.Request;
import models.requests.client.GetRequest;
import models.requests.client.StatRequest;
import models.requests.server.ListRequest;
import models.requests.server.SourcesRequest;
import models.requests.server.UpdateRequest;
import models.requests.server.UploadRequest;
import models.response.Response;
import models.response.client.GetResponse;
import models.response.client.StatResponse;
import models.response.server.ListResponse;
import models.response.server.SourcesResponse;
import models.response.server.UpdateResponse;
import models.response.server.UploadResponse;
import models.torrent.TorrentClient;
import models.torrent.TorrentClientState;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class TorrentClientImpl implements TorrentClient {
    private final String serverHost;
    private Thread updateThread;
    private Thread downloadThread;
    private Thread localServerThread;
    private TorrentClientState tcs;
    private volatile boolean shutdown = false;

    public TorrentClientImpl(String serverHost, int clientId, boolean cleanState) throws IOException {
        this.serverHost = serverHost;
        ServerSocket server = new ServerSocket(0);
        tcs = new TorrentClientState(server, clientId, cleanState);
        updateRun();
        downloadRun();
        localServerRun();
    }

    @Override
    public List<TorrentFile> distributedFiles() {
        return tcs.getOwnFiles().values().stream().collect(Collectors.toList());
    }

    @Override
    public void registerFile(File file) {
        Request uploadRequest = new UploadRequest(file.getName(), file.length());
        UploadResponse uploadResponse = new UploadResponse();

        if (!sendServerRequest(uploadRequest, uploadResponse)) {
            throw new TorrentException("Cannot connection to server");
        }

        int fileId = uploadResponse.getFileId();
        tcs.getOwnFiles().put(fileId, new TorrentFile(fileId, file.getName(), file.length(), true));
    }

    private boolean sendServerRequest(Request request, Response response) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(serverHost, getServerPort()), 5000);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            request.writeToDataOutputStream(dos);
            response.readFromDataInputStream(dis);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean sendClientRequest(Request request, Response response, byte[] targetIp, short targetPort) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(InetAddress.getByAddress(targetIp), Short.MAX_VALUE - targetPort), 5000);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            request.writeToDataOutputStream(dos);
            response.readFromDataInputStream(dis);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public void shutdown() throws IOException {
        shutdown = true;
        updateThread.interrupt();
        downloadThread.interrupt();
        tcs.getServer().close();
        tcs.saveState();
    }

    @Override
    public void forceUpdate() {
        Request updateRequest = new UpdateRequest(tcs);
        Response updateResponse = new UpdateResponse();
        try {
            sendServerRequest(updateRequest, updateResponse);
        } catch (TorrentException e) {
            System.out.println("Cannot forceUpdate information: " + e.getMessage());
        }
        System.out.println("Information updated");
    }

    @Override
    public boolean addGetTask(int id, String location) {
        Optional<TorrentFile> targetFile = listFiles().stream().filter(x -> x.getFileId() == id).findAny();
        if (!targetFile.isPresent()) {
            return false;
        }
        TorrentFile target = targetFile.get();
        target.setName(location);
        tcs.getOwnFiles().putIfAbsent(id, target);
        return true;
    }

    @Override
    public List<TorrentFile> listFiles() {
        Request listRequest = new ListRequest();
        ListResponse listResponse = new ListResponse();

        sendServerRequest(listRequest, listResponse);

        return listResponse.getFilesList();
    }

    private boolean forceDownload() {
        List<TorrentFile> targetFiles = tcs.getOwnFiles().values().stream().filter(it -> !it.isDownload()).collect(Collectors.toList());
        boolean was = false;
        for (TorrentFile file : targetFiles) {
            Request sourcesRequest = new SourcesRequest(file.getFileId());
            SourcesResponse sourcesResponse = new SourcesResponse();

            sendServerRequest(sourcesRequest, sourcesResponse);


            try (RandomAccessFile raf = new RandomAccessFile(file.getName(), "rw")) {
                for (TorrentPeer peer : sourcesResponse.getPeersList()) {
                    Request statRequest = new StatRequest(file.getFileId());
                    StatResponse statResponse = new StatResponse();

                    if (!sendClientRequest(statRequest, statResponse, peer.getPeerIp(), peer.getPeerPort())) {
                        continue;
                    }


                    for (Integer missingPartId : file.getMissingPieces()) {
                        if (statResponse.getPartsList().contains(missingPartId)) {
                            Request getRequest = new GetRequest(file.getFileId(), missingPartId);
                            GetResponse getResponse = new GetResponse(file.getPieceSizeById(missingPartId));

                            if (!sendClientRequest(getRequest, getResponse, peer.getPeerIp(), peer.getPeerPort())) {
                                continue;
                            }
                            raf.seek(missingPartId * file.getPieceSize());
                            raf.write(getResponse.getContent(), 0, getResponse.getContentSize());
                            file.getPieces().add(missingPartId);
                            was = true;
                        }
                    }
                }
            } catch (IOException e) {
                throw new TorrentException("Error occur in file IO", e);
            }
        }
        return was;
    }

    private void forceLocalServerResponse(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        DataInputStream dis = new DataInputStream(is);
        OutputStream os = socket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);

        if (socket.isConnected() && !socket.isInputShutdown()) {
            Command cmd = ClientCommandBuilder.build(tcs, dis);
            cmd.evaluateCommand(dos);
            dos.flush();
            socket.close();
        }
    }

    private void updateRun() {
        updateThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                forceUpdate();
                try {
                    Thread.sleep((long) (4.5 * 60 * 1000));
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updateThread.start();
    }

    private void downloadRun() {
        downloadThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                boolean result = forceDownload();
                if (!result) {
                    try {
                        Thread.sleep((long) (25));
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        downloadThread.start();
    }

    private void localServerRun() {
        localServerThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Socket socket = tcs.getServer().accept();
                    forceLocalServerResponse(socket);
                } catch (SocketException e) {
                    if (!shutdown) {
                        throw new TorrentException("SocketException: ", e);
                    }
                    return;
                } catch (IOException e) {
                    throw new TorrentException("IOException: ", e);
                }
            }
        });
        localServerThread.start();
    }
}
