package builders;

import proto.TestStrategyOuterClass.TestStrategy;
import servers.*;

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
                return new TcpPermanentConnectionNonBlock();
            case TCP_NEW_CONNECTION_SINGLE_THREAD:
                return new TcpSingleThread();
            case TCP_ASYNC:
                return new TcpAsync();
            case UDP_NEW_THREAD:
                return new UdpNewThread();
            case UDP_FIXED_POOL:
                return new UdpFixedPool();
        }
        throw new UnexpectedException("Unrecognized client");
    }
}
