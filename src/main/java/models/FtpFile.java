package models;


public class FtpFile {
    private boolean isDirectory;
    private String path;

    public FtpFile(boolean isDirectory, String path) {
        this.isDirectory = isDirectory;
        this.path = path;
    }

    public String getName() {
        return path;
    }

    @Override
    public String toString() {
        return getName() + " " + isDirectory;
    }
}
