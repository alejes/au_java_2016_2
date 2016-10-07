package models.requests;


public class ListRequest implements Request {
    private String path;

    public ListRequest(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "1 " + path;
    }

    public byte[] toByteArray() {
        return toString().getBytes();
    }
}
