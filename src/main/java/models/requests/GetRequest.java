package models.requests;


public class GetRequest {
    private String path;

    public GetRequest(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "2 " + path;
    }
}
