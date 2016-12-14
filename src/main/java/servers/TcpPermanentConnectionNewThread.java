package servers;

import proto.ServerResponseStatMessageOuterClass.ServerResponseStatMessage;

import java.io.IOException;


public class TcpPermanentConnectionNewThread extends Server {
    public TcpPermanentConnectionNewThread() throws IOException {
        super();
    }

    @Override
    public ServerResponseStatMessage stopAndCollectStatistic() {
        return ServerResponseStatMessage.newBuilder()
                .setQueryProcessingTime(33)
                .setClientProcessingTime(55)
                .build();
    }

    @Override
    public void run() {

    }
}
