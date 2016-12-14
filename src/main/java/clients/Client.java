package clients;


import proto.ClientResponseStatMessageOuterClass.ClientResponseStatMessage;

public abstract class Client implements Runnable {
    public abstract ClientResponseStatMessage collectStatistic();
}
