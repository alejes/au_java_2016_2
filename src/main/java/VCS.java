import db.DBDriver;
import db.SQLLite;
import exceptions.VCSException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.SQLException;

public class VCS {

    private DBDriver db;

    public enum MODIFY_ACTION {
        CREATE,
        DELETE
    }

    private static FileFilter filter = pathname -> !pathname.getAbsoluteFile().toString().endsWith("\\.vcs");

    public VCS() {
        db = new SQLLite();
    }

    public void initRepository() {
        try {
            FileUtils.deleteDirectory(new File("./.vcs"));
        } catch (IOException e) {
            System.out.printf("Cannot get stat of directory");
        }
        try {
            File vcs = new File("./.vcs");
            if (!vcs.mkdir()) {
                throw new VCSException("Cannot initialize .vcs directory");
            }
            db.connect();
            db.initTables();
            db.addBranch("master");
            db.switchBranch("master");

        } catch (ClassNotFoundException e) {
            System.out.printf("Cannot find SQLite");
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQL exception:" + e.getMessage());
        }
    }

    public void checkout(String branchName, boolean create) {
        try {
            db.connect();
            if (create) {
                db.addBranch(branchName);
            } else {
                DBDriver.CommitResult commitData = db.getLastCommit(branchName);
                String commitDirectory = "./.vcs/" + commitData.branchId + "/" + commitData.commitId;
                System.out.println(commitDirectory);
                File vcs = new File(commitDirectory);
                File currentDirectory = new File(".");
                for (File file : currentDirectory.listFiles(filter)) {
                    if (file.isFile()) {
                        file.delete();
                    } else {
                        FileUtils.deleteDirectory(file);
                    }
                }

                FileUtils.copyDirectory(vcs, new File("."), filter);
            }
            db.switchBranch(branchName);
        } catch (ClassNotFoundException e) {
            System.out.printf("Cannot find SQLite");
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQL exception:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Cannot switch files during checkout:");
        }
    }

    public void branch(String branchName, MODIFY_ACTION action) {
        try {
            db.connect();
            switch (action) {
                case CREATE:
                    db.addBranch(branchName);
                    break;
                case DELETE:
                    db.deleteBranch(branchName);
                    break;
            }
        } catch (ClassNotFoundException e) {
            System.out.printf("Cannot find SQLite");
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQL exception:" + e.getMessage());
        }
    }

    public void commit(String message) {
        try {
            db.connect();
            DBDriver.CommitResult commitData = db.commit(message);

            String commitDirectory = "./.vcs/" + commitData.branchId + "/" + commitData.commitId;
            File vcs = new File(commitDirectory);
            if (!vcs.mkdirs()) {
                throw new VCSException("Cannot initialize branch directory");
            }
            FileUtils.copyDirectory(new File("."), vcs, filter);
        } catch (ClassNotFoundException e) {
            System.out.printf("Cannot find SQLite");
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQL exception:" + e.getMessage());
        } catch (IOException e) {
            System.out.printf("Cannot copy commit files");
        }
    }

    public void log() {
        try {
            db.connect();
            String log = db.log();
            System.out.printf(log);
        } catch (ClassNotFoundException e) {
            System.out.printf("Cannot find SQLite");
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQL exception:" + e.getMessage());
        }
    }
}
