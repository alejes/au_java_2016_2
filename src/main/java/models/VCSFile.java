package models;


public class VCSFile {
    public VCSEntity entity;
    public ModifyAction action;

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

