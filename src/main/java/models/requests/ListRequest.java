package models.requests;


public class ListRequest {
    private String path;

    public ListRequest(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "1 " + path;
    }
}
