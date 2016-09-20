import db.DBDriver;
import db.SQLLite;
import exceptions.VCSException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.min;

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
                if (commitData == null) {
                    commitData = db.getCommitById(branchName);
                }
                String commitDirectory = "./.vcs/" + commitData.branchId + "/" + commitData.commitId;

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

    public void merge(String sourceBranch) {
        try {
            db.connect();
            DBDriver.CommitResult commitData = db.getLastCommit(sourceBranch);
            String commitDirectory = "./.vcs/" + commitData.branchId + "/" + commitData.commitId;

            mergeDirectory(new File("."), new File(commitDirectory));
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

    private void mergeDirectory(File target, File source) throws IOException {
        Map<String, File> fileMap = Arrays.stream(source.listFiles(filter)).collect(Collectors.toMap(File::getName, file -> file));
        for (File file : target.listFiles(filter)) {
            if (fileMap.containsKey(file.getName())) {
                if (file.isDirectory()) {
                    mergeDirectory(file, fileMap.get(file.getName()));
                } else {
                    mergeFiles(file, fileMap.get(file.getName()));
                }
                fileMap.remove(file.getName());
            }
        }
        for (File file : fileMap.values()) {
            String targetName = target.getAbsolutePath() + "/" + file.getName();
            if (file.isDirectory()) {
                FileUtils.copyDirectory(file, new File(targetName));
            } else {
                FileUtils.copyFile(file, new File(targetName));
            }
        }
    }

    private void mergeFiles(File target, File source) throws IOException {
        List<String> targetLines = Arrays.asList(new String(Files.readAllBytes(target.toPath())).split("\n"));
        List<String> sourceLines = Arrays.asList(new String(Files.readAllBytes(source.toPath())).split("\n"));
        StringBuilder resultLines = new StringBuilder();
        final int lastLine = min(targetLines.size(), sourceLines.size());

        for (int pointer = 0; pointer < lastLine; ++pointer) {
            int startPointer = pointer;
            while ((pointer < lastLine) && !targetLines.get(pointer).equals(sourceLines.get(pointer))) {
                ++pointer;
            }
            if (startPointer < pointer) {
                for (int i = startPointer; i < pointer; ++i) {
                    resultLines.append("-" + targetLines.get(i) + "\n");
                }
                for (int i = startPointer; i < pointer; ++i) {
                    resultLines.append("+" + sourceLines.get(i) + "\n");
                }
            }
            if (pointer < targetLines.size()) {
                resultLines.append(targetLines.get(pointer) + "\n");
            }
        }

        for (int pointer = lastLine; pointer < targetLines.size(); ++pointer) {
            resultLines.append('-' + targetLines.get(pointer) + '\n');
        }

        for (int pointer = lastLine; pointer < sourceLines.size(); ++pointer) {
            resultLines.append('+' + sourceLines.get(pointer) + '\n');
        }
        resultLines.deleteCharAt(resultLines.length() - 1);
        Files.write(target.toPath(), resultLines.toString().getBytes());
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
            String currentBranch = db.getCurrentBranch();
            System.out.println("On branch " + currentBranch + ":");
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
