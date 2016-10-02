import db.DBDriver;
import db.SQLLite;
import exceptions.VCSException;
import models.CommitResult;
import models.FILE_ACTION;
import models.VCSEntity;
import models.VCSFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.min;

public class VCS {
    private static String rootDirectory = Paths.get(".").resolve(".").normalize().toAbsolutePath().toString();

    private static FileFilter weakFilter = pathname -> !pathname.getAbsoluteFile().toString().endsWith(".vcs");
    private static FileFilter strongFilter = pathname -> !pathname.getAbsoluteFile().toString().endsWith(".vcs");

    private static String resolveFilePath(String path) {
        return resolveFilePath(path, rootDirectory);
    }

    private static String resolveFilePath(String path, String base) {
        return Paths.get(base).normalize().toAbsolutePath().relativize(Paths.get(base).resolve(path).normalize().toAbsolutePath()).toString();
    }

    private static String vcsDirectory = "./.vcs";
    private static String stageDirectory = vcsDirectory + "/stage";
    private static String deleteDirectory = vcsDirectory + "/deleted";
    private static String filesDirectory = vcsDirectory + "/files";
    private DBDriver db;

    public VCS() {
        db = new SQLLite();
    }

    private static List<String> getDirectoryFiles(File directory, String base, FileFilter filter) {
        List<String> result = Arrays.stream(directory.listFiles(filter)).filter(File::isDirectory).map((it) -> getDirectoryFiles(it, base)).flatMap(List::stream).collect(Collectors.toList());
        result.addAll(Arrays.stream(directory.listFiles(filter)).filter((it) -> !it.isDirectory()).map((it) -> resolveFilePath(it.getAbsolutePath(), base)).collect(Collectors.toList()));
        return result;
    }

    private static List<String> getDirectoryFiles(File directory, String base) {
        return getDirectoryFiles(directory,base, weakFilter);
    }

    private static boolean isFilesEquals(File first, File second) throws IOException {
        String firstContent = new String(Files.readAllBytes(first.toPath()));
        String secondContent = new String(Files.readAllBytes(second.toPath()));

        return firstContent.equals(secondContent);
    }

    public void initRepository() {
        try {
            FileUtils.deleteDirectory(new File(VCS.vcsDirectory));
        } catch (IOException e) {
            System.out.printf("Cannot get stat of directory");
        }
        try {
            File vcs = new File(VCS.vcsDirectory);
            if (!vcs.mkdir()) {
                throw new VCSException("Cannot initialize .vcs directory");
            }
            File files = new File(VCS.filesDirectory);
            if (!files.mkdir()) {
                throw new VCSException("Cannot initialize files directory");
            }

            File stage = new File(VCS.stageDirectory);
            if (!stage.mkdir()) {
                throw new VCSException("Cannot initialize stage directory");
            }

            File delete = new File(VCS.deleteDirectory);
            if (!delete.mkdir()) {
                throw new VCSException("Cannot initialize delete directory");
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
                CommitResult commitData = db.getLastCommit(branchName);
                if (commitData == null) {
                    commitData = db.getCommitById(branchName);
                }
                if (commitData == null) {
                    throw new VCSException("Not found target of checkout");
                }

                Set<VCSEntity> commitFiles = db.commitFiles(commitData);

                File currentDirectory = new File(".");
                for (File file : currentDirectory.listFiles(weakFilter)) {
                    if (file.isFile()) {
                        file.delete();
                    } else {
                        FileUtils.deleteDirectory(file);
                    }
                }

                for (VCSEntity entity : commitFiles) {
                    FileUtils.copyFile(new File(VCS.filesDirectory +"/"+ entity.fileId), new File(entity.path));
                }
            }
            db.switchBranch(branchName);
        } catch (ClassNotFoundException e) {
            System.out.printf("Cannot find SQLite");
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQL exception:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Cannot switch files during checkout:" + e.getMessage());
        }
    }

    public void merge(String sourceBranch) {
        try {
            db.connect();
            CommitResult commitData = db.getLastCommit(sourceBranch);
            String commitDirectory = VCS.vcsDirectory + "/" + commitData.branchId + "/" + commitData.commitId;

            mergeDirectory(new File("."), new File(commitDirectory));
        } catch (ClassNotFoundException e) {
            System.out.printf("Cannot find SQLite");
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQL exception:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Cannot switch files during checkout: " + e.getMessage());
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
                    Integer branchId = db.getBranchId(branchName);
                    if (branchId == null) {
                        throw new VCSException("Cannot found branch " + branchName);
                    }
                    db.deleteBranch(branchName);
                    FileUtils.deleteDirectory(new File(VCS.vcsDirectory + "/" + branchId));
                    break;
            }
        } catch (ClassNotFoundException e) {
            System.out.printf("Cannot find SQLite");
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQL exception:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Cannot delete branch");
        }
    }

    public void commit(String message) {
        try {
            db.connect();
            String currentBranch = db.getCurrentBranch();
            CommitResult lastCommit = null;
            if (currentBranch != null) {
                lastCommit = db.getLastCommit(currentBranch);
            }
            Set<VCSEntity> lastCommitFiles = db.commitFiles(lastCommit);
            Set<VCSEntity> deletedFiles = getDirectoryFiles(new File(VCS.deleteDirectory), VCS.deleteDirectory).stream().map((it) -> new VCSEntity(0, it)).collect(Collectors.toSet());
            CommitResult commitData = db.commit(message);
            File stage = new File(VCS.stageDirectory);
            Set<VCSEntity> stageEntities = registerDirectory(stage, ".");
            stageEntities.removeAll(deletedFiles);

            for (VCSEntity entity : stageEntities) {
                db.addFileToCommit(commitData.commitId, entity.fileId);
                lastCommitFiles.remove(entity);
            }

            for (VCSEntity entity : lastCommitFiles) {
                db.addFileToCommit(commitData.commitId, entity.fileId);
            }

            FileUtils.deleteDirectory(new File(VCS.stageDirectory));
            FileUtils.forceMkdir(new File(VCS.stageDirectory));
            FileUtils.deleteDirectory(new File(VCS.deleteDirectory));
            FileUtils.forceMkdir(new File(VCS.deleteDirectory));
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

    public void status() {
        try {
            db.connect();
            String currentBranch = db.getCurrentBranch();
            System.out.println("On branch " + currentBranch + ":");

            List<String> stageFiles = getDirectoryFiles(new File(VCS.stageDirectory), VCS.stageDirectory);

            if (!stageFiles.isEmpty()) {
                System.out.println("Changes to be committed:");
                stageFiles.forEach(System.out::println);
            }

            List<String> deletedFiles = getDirectoryFiles(new File(VCS.deleteDirectory), VCS.deleteDirectory);
            if (!deletedFiles.isEmpty()) {
                System.out.println("Files will be deleted:");
                deletedFiles.forEach(System.out::println);
            }

            CommitResult lastCommit = null;
            if (currentBranch != null) {
                lastCommit = db.getLastCommit(currentBranch);
            }

            Map<String, Integer> lastCommitFiles = db.commitFiles(lastCommit).stream().collect(Collectors.toMap((x) -> x.path, (y) -> y.fileId));
            List<VCSEntity> allFiles = getDirectoryFiles(new File("."), ".", strongFilter).stream().map((it) -> new VCSEntity(-1, it)).collect(Collectors.toList());


            List<VCSFile> notStagedChanges = new ArrayList<>();
            for (VCSEntity entity : allFiles) {
                File compareTarget = null;
                File stageFile = new File(VCS.stageDirectory + "/" + entity.path);
                if (stageFile.exists()) {
                    compareTarget = stageFile;
                } else if (lastCommitFiles.containsKey(entity.path)) {
                    compareTarget = new File(VCS.filesDirectory + "/" + lastCommitFiles.get(entity.path));
                }

                if (compareTarget == null) {
                    notStagedChanges.add(new VCSFile(entity, FILE_ACTION.NEW));
                } else if (!isFilesEquals(new File("./" + entity.path), compareTarget)) {
                    notStagedChanges.add(new VCSFile(entity, FILE_ACTION.MODIFIED));
                }
            }

            if (!notStagedChanges.isEmpty()) {
                System.out.println("Changes not staged for commit:");
                notStagedChanges.forEach((it) -> System.out.println(it.action.name() + " " + it.entity.path));
            }
        } catch (ClassNotFoundException e) {
            System.out.printf("Cannot find SQLite");
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQL exception:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Cannot compare files");
        }
    }

    public void addFileToStage(String path) {
        path = resolveFilePath(path);

        File source = new File("./" + path);
        if (!source.exists()) {
            throw new VCSException("file " + path + " not found");
        }

        try {
            db.connect();
            if (wasFileChanged(path)) {
                FileUtils.copyFile(source, new File(VCS.stageDirectory + "/" + path));
            }
        } catch (ClassNotFoundException e) {
            System.out.printf("Cannot find SQLite");
        } catch (IOException e) {
            System.out.printf("Cannot copy file to stage");
        } catch (SQLException e) {
            System.out.println("SQL exception:" + e.getMessage());
        }
    }

    public void resetFileInStage(String path) {
        try {
            FileUtils.moveFile(new File(VCS.deleteDirectory + "/" + path), new File("./" + path));
        } catch (IOException e) {
            System.out.printf("Cannot delete file from stage");
        }
    }

    public void removeFileInStage(String path) {
        path = resolveFilePath(path);
        File source = new File("./" + path);
        if (!source.exists()) {
            throw new VCSException("file " + path + " not found");
        }

        try {
            FileUtils.moveFile(source, new File(VCS.deleteDirectory + "/" + path));
        } catch (IOException e) {
            System.out.printf("Cannot delete file to stage");
        }
    }

    private Set<VCSEntity> registerDirectory(File sourceDirectory, String prefix) throws SQLException, IOException {
        Set<VCSEntity> hs = new HashSet<>();
        for (File file : sourceDirectory.listFiles(weakFilter)) {
            String stageFilePath = prefix + "/" + file.getName();
            if (file.isDirectory()) {
                hs.addAll(registerDirectory(file, stageFilePath));
            } else {
                String resolvedPath = resolveFilePath(stageFilePath);
                int fileId = db.registerFile(resolvedPath);
                FileUtils.copyFile(file, new File(VCS.filesDirectory + "/" + fileId));
                hs.add(new VCSEntity(fileId, resolvedPath));
            }
        }
        return hs;
    }

    private void mergeDirectory(File target, File source) throws IOException {
        Map<String, File> fileMap = Arrays.stream(source.listFiles(weakFilter)).collect(Collectors.toMap(File::getName, file -> file));
        for (File file : target.listFiles(weakFilter)) {
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
                    resultLines.append("+" + targetLines.get(i) + "\n");
                }
                for (int i = startPointer; i < pointer; ++i) {
                    resultLines.append("-" + sourceLines.get(i) + "\n");
                }
            }
            if (pointer < lastLine) {
                resultLines.append(targetLines.get(pointer) + "\n");
            }
        }

        for (int pointer = lastLine; pointer < targetLines.size(); ++pointer) {
            resultLines.append('+' + targetLines.get(pointer) + '\n');
        }

        for (int pointer = lastLine; pointer < sourceLines.size(); ++pointer) {
            resultLines.append('-' + sourceLines.get(pointer) + '\n');
        }
        resultLines.deleteCharAt(resultLines.length() - 1);
        Files.write(target.toPath(), resultLines.toString().getBytes());
    }

    private boolean wasFileChanged(String path) throws SQLException, IOException {
        String currentBranch = db.getCurrentBranch();
        CommitResult lastCommitId = null;
        if (currentBranch != null) {
            lastCommitId = db.getLastCommit(currentBranch);
        }

        if (lastCommitId == null) {
            return true;
        }

        Integer fileId = db.getFileIdInCommit(lastCommitId.commitId, path);
        if (fileId == null) {
            return true;
        }
        return !isFilesEquals(new File("." + path), new File(VCS.filesDirectory + "/" + fileId));
    }

    public enum MODIFY_ACTION {
        CREATE,
        DELETE
    }
}
