package clients;


import proto.ClientInitMessageOuterClass.ClientInitMessage;
import proto.ClientResponseStatMessageOuterClass.ClientResponseStatMessage;

import java.util.Random;

public abstract class Client implements Runnable {
    protected ClientInitMessage initMessage;
    protected long averageClientTime;
    protected int[] array;

    public long getAverageClientTime() {
        return averageClientTime;
    }

    public void initClientData(ClientInitMessage initMessage) {
        this.initMessage = initMessage;
        array = new Random().ints(initMessage.getN()).toArray();
    }
}
