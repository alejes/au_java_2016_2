package models;

public final class VCSEntity {
    public int fileId;
    public String path;

    public VCSEntity(int fileId, String path) {
        this.fileId = fileId;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null){
            return false;
        }
        else if (!(o instanceof VCSEntity)){
            return false;
        }
        return this.path.equals(((VCSEntity) o).path);
    }

    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

    @Override
    public String toString() {
        return "VCSEntity{" +
                "fileId=" + fileId +
                ", path='" + path + '\'' +
                '}';
    }
}
