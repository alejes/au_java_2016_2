package models.responses;

import models.Networkable;

public class GetResponse implements Networkable {
    private final int size;
    private final byte[] content;

    public GetResponse(int size, byte[] content) {
        this.size = size;
        this.content = content;
    }

    public GetResponse(String stringResponse) {
        int spaceIndex = stringResponse.indexOf(' ');
        if (spaceIndex < 0) {
            spaceIndex = stringResponse.length();
        }
        size = Integer.valueOf(stringResponse.substring(0, spaceIndex));
        content = stringResponse.substring(spaceIndex + 1).getBytes();
    }

    public byte[] getBytes() {
        return content;
    }

    @Override
    public String toString() {
        return size + " " + new String(content);
    }

    @Override
    public String toNetworkResponse() {
        return size + " " + new String(content);
    }
}
