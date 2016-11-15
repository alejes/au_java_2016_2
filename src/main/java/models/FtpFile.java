package models;


public class FtpFile {
    private final boolean isDirectory;
    private final String path;

    public FtpFile(boolean isDirectory, String path) {
        this.isDirectory = isDirectory;
        this.path = path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getName() {
        return path;
    }

    @Override
    public String toString() {
        return (isDirectory ? ">" : "") + getName();
    }

}
