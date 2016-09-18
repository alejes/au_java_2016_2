import db.DBDriver;
import db.SQLLite;
import exceptions.VCSException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class VCS {
    private DBDriver db;

    public enum MODIFY_ACTION {
        CREATE,
        DELETE
    }

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
            File vsc = new File("./.vcs");
            if (!vsc.mkdir()) {
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
            }
            db.switchBranch(branchName);
        } catch (ClassNotFoundException e) {
            System.out.printf("Cannot find SQLite");
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQL exception:" + e.getMessage());
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

    public void commit(String message){

    }

}
