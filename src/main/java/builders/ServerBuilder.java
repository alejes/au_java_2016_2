package builders;

import proto.TestStrategyOuterClass.TestStrategy;
import servers.Server;
import servers.TcpPermanentConnectionCache;
import servers.TcpPermanentConnectionNewThread;

import java.io.IOException;
import java.rmi.UnexpectedException;

public class ServerBuilder {
    public static Server buildServer(TestStrategy ts) throws IOException {
        switch (ts) {
            case TCP_PERMANENT_CONNECTION_NEW_THREAD:
                return new TcpPermanentConnectionNewThread();
            case TCP_PERMANENT_CONNECTION_CACHE:
                return new TcpPermanentConnectionCache();
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
