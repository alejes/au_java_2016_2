package models;


public class VCSFile {
    public VCSEntity entity;
    public FILE_ACTION action;

    public VCSFile(VCSEntity entity, FILE_ACTION action) {
        this.entity = entity;
        this.action = action;
    }

    @Override
    public String toString() {
        return "VCSFile{" +
                "entity=" + entity +
                ", action=" + action +
                '}';
    }
}
