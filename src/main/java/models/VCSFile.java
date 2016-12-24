package models;


public class VCSFile {
    public final VCSEntity entity;
    public final ModifyAction action;

    public VCSFile(VCSEntity entity, ModifyAction action) {
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

