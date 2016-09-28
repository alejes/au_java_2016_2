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
        return "[VCSFile: new=" + currentVersion.getAbsolutePath() + "; old=" +
                oldVersion.getAbsolutePath() + " action= " + action.name() + "]";
    }
}
