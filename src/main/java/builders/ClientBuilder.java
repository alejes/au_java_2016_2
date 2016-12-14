package builders;

import clients.Client;
import clients.TcpPermanentConnectionNewThread;
import proto.TestStrategyOuterClass.TestStrategy;

import java.io.IOException;
import java.rmi.UnexpectedException;

public class ClientBuilder {
    public static Client buildClient(TestStrategy ts) throws IOException {
        switch (ts) {
            case TCP_PERMANENT_CONNECTION_NEW_THREAD:
                return new TcpPermanentConnectionNewThread();
            case TCP_PERMANENT_CONNECTION_CACHE:
                break;
            case TCP_PERMANENT_CONNECTION_NON_BLOCK:
                break;
            case TCP_NEW_CONNECTION_SINGLE_THREAD:
                break;
            case TCP_ASYNC:
                break;
            case UDP_NEW_THREAD:
                break;
            case UDP_FIXED_POOL:
                break;
            case UNRECOGNIZED:
                break;
        }
        throw new UnexpectedException("w");
    }
}
