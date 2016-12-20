package builders;

import clients.Client;
import clients.TCPNewConnectionClient;
import clients.TCPPermanentConnectionClient;
import clients.UdpNewConnectionClient;
import proto.TestStrategyOuterClass.TestStrategy;

import java.io.IOException;
import java.rmi.UnexpectedException;

public class ClientBuilder {
    public static Client buildClient(TestStrategy ts) throws IOException {
        switch (ts) {
            case TCP_PERMANENT_CONNECTION_NEW_THREAD:
            case TCP_PERMANENT_CONNECTION_CACHE:
            case TCP_PERMANENT_CONNECTION_NON_BLOCK:
                return new TCPPermanentConnectionClient();
            case TCP_NEW_CONNECTION_SINGLE_THREAD:
            case TCP_ASYNC:
                return new TCPNewConnectionClient();
            case UDP_NEW_THREAD:
            case UDP_FIXED_POOL:
                return new UdpNewConnectionClient();
        }
        throw new UnexpectedException("Unrecognized client");
    }
}
