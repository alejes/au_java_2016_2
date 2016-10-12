package models.requests;


public class GetRequest implements Request{
    private String path;

    public GetRequest(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "2 " + path;
    }

    public byte[] toByteArray(){
        return toString().getBytes();
    }
}
