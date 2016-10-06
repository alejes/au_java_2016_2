package models.responses;

import java.util.Arrays;


public class GetResponse {
    private int size;
    private Byte[] content;

    public GetResponse(int size, Byte[] content) {
        this.size = size;
        this.content = content;
    }

    @Override
    public String toString() {
        return size + " " + Arrays.toString(content);
    }
}
