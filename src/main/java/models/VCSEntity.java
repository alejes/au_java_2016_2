package models;

public final class VCSEntity {
    public final int fileId;
    public final String path;

    public VCSEntity(int fileId, String path) {
        this.fileId = fileId;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof String) {
            return path.equals(o);
        } else if (!(o instanceof VCSEntity)) {
            return false;
        }
        return path.equals(((VCSEntity) o).path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "VCSEntity{" +
                "fileId=" + fileId +
                ", path='" + path + '\'' +
                '}';
    }
}
