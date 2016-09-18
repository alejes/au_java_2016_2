import db.DBDriver;
import db.SQLLite;
import exceptions.VCSException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


public class vcs {
    public static void checkArgumentsLength(String[] args, int minimumCount, String message) throws VCSException {
        if (args.length < minimumCount) {
            throw new VCSException(message);
        }
    }

    public static void main(String[] args) {
        if (args.length <= 0) {
            System.out.println("Help");
            return;
        }
        //Options options = new Options();
        //options.addOption("h", "help", false, "show help.");
        //options.addOption("v", "var", true, "Here you can set parameter .");
        DBDriver db = new SQLLite();
        try {
            switch (args[0]) {
                case "init":
                    try {
                        FileUtils.deleteDirectory(new File("./.vcs"));
                    } catch (IOException e) {
                        System.out.printf("Cannot get stat of directory");
                    }
                    File vsc = new File("./.vcs");
                    if (!vsc.mkdir()) {
                        throw new VCSException("Cannot initialize .vcs directory");
                    }
                    db.connect();
                    db.initTables();

                    break;

                case "checkout":
                    checkArgumentsLength(args, 2, "checkout require at least 1 arguments - branch name. " +
                            "You can use -b and -d modificators for create and delete branch");
                    db.connect();
                    switch (args[1]) {
                        case "-b":
                            checkArgumentsLength(args, 3, "Please specify branch name");
                            db.addBranch(args[2]);
                            break;
                    }

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
}

