package models;


import java.io.File;

public class VCSFile {
    public File currentVersion;
    public File oldVersion;
    public FILE_ACTION action;

    public VCSFile(File currentVersion, File oldVersion, FILE_ACTION action) {
        this.currentVersion = currentVersion;
        this.oldVersion = oldVersion;
        this.action = action;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[VCSFile: new=");
        if (currentVersion == null) {
            result.append("null");
        } else {
            result.append(currentVersion.getAbsolutePath());
        }
        result.append("; old=");
        if (oldVersion == null) {
            result.append("null");
        } else {
            result.append(oldVersion.getAbsolutePath());
        }
        result.append(" action= ");
        result.append(action.name());
        result.append("]");
        return result.toString();
    }
}
