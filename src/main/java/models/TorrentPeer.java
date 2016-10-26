package models;

import com.sun.istack.internal.NotNull;

import java.util.Arrays;

public class TorrentPeer {
    @NotNull
    private final byte[] peerIp;
    @NotNull
    private final short peerPort;

    public TorrentPeer(byte[] peerIp, short peerPort) {
        this.peerIp = peerIp;
        this.peerPort = peerPort;
    }

    public byte[] getPeerIp() {
        return peerIp;
    }

    public short getPeerPort() {
        return peerPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TorrentPeer)) return false;

        TorrentPeer that = (TorrentPeer) o;

        if (peerPort != that.peerPort) return false;
        return Arrays.equals(peerIp, that.peerIp);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(peerIp);
        result = 31 * result + (int) peerPort;
        return result;
    }
}
