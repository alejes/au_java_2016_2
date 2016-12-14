package clients;

import proto.ClientResponseStatMessageOuterClass.ClientResponseStatMessage;

import java.io.IOException;


public class TcpPermanentConnectionNewThread extends Client {
    public TcpPermanentConnectionNewThread() throws IOException {
        super();
    }

    @Override
    public ClientResponseStatMessage collectStatistic() {
        return ClientResponseStatMessage.newBuilder()
                .setAverageClientTime(12)
                .build();
    }

    @Override
    public void run() {

    }
}
