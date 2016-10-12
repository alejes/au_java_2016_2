package models;


public class FtpFile implements Networkable {
    private final boolean isDirectory;
    private final String path;

    public FtpFile(boolean isDirectory, String path) {
        this.isDirectory = isDirectory;
        this.path = path;
    }

    public String getName() {
        return path;
    }

    @Override
    public String toString() {
        return ((isDirectory) ? ">" : "") + getName();
    }

    @Override
    public String toNetworkResponse() {
        return getName() + " " + isDirectory;
    }
}
