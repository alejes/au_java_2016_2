import db.DBDriver;
import db.SQLLite;
import exceptions.VCSException;
import models.CommitResult;
import models.ModifyAction;
import models.VCSEntity;
import models.VCSFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.min;

public class VCS {
    private static final String rootDirectory = Paths.get(".").resolve(".").normalize().toAbsolutePath().toString();
    private static final FileFilter weakFilter = pathname -> !pathname.getAbsoluteFile().toString().endsWith(".vcs");
    private static final FileFilter strongFilter = pathname -> !pathname.getAbsoluteFile().toString().endsWith(".vcs");
    private static final String VCS_DIRECTORY = "./.vcs";
    private static final String STAGE_DIRECTORY = VCS_DIRECTORY + "/stage";
    private static final String DELETE_DIRECTORY = VCS_DIRECTORY + "/deleted";
    private static final String FILES_DIRECTORY = VCS_DIRECTORY + "/files";
    private final DBDriver db;

    public VCS() {
        db = new SQLLite();
    }

    private static String resolveFilePath(String path) {
        return resolveFilePath(path, rootDirectory);
    }

    private static String resolveFilePath(String path, String base) {
        return Paths.get(base).normalize().toAbsolutePath()
                .relativize(Paths.get(base).resolve(path).normalize().toAbsolutePath()).toString();
    }

    private static List<String> getDirectoryFiles(File directory, String base, FileFilter filter) {
        List<String> result = new ArrayList<>();
        File[] filesInDirectory = directory.listFiles(filter);
        if (filesInDirectory != null) {
            result.addAll(Arrays.stream(filesInDirectory).filter(File::isDirectory)
                    .map((it) -> getDirectoryFiles(it, base)).flatMap(List::stream).collect(Collectors.toList()));
            result.addAll(Arrays.stream(filesInDirectory).filter((it) -> !it.isDirectory())
                    .map((it) -> resolveFilePath(it.getAbsolutePath(), base)).collect(Collectors.toList()));
        }
        return result;
    }

    private static List<String> getDirectoryFiles(File directory, String base) {
        return getDirectoryFiles(directory, base, weakFilter);
    }

    private static boolean isFilesEquals(File first, File second) throws IOException {
        String firstContent = new String(Files.readAllBytes(first.toPath()));
        String secondContent = new String(Files.readAllBytes(second.toPath()));

        return firstContent.equals(secondContent);
    }

    public void initRepository() {
        try {
            FileUtils.deleteDirectory(new File(VCS.VCS_DIRECTORY));
        } catch (IOException e) {
            throw new VCSException("Cannot get stat of directory", e);
        }
        try {
            File vcs = new File(VCS.VCS_DIRECTORY);
            if (!vcs.mkdir()) {
                throw new VCSException("Cannot initialize .vcs directory");
            }
            File files = new File(VCS.FILES_DIRECTORY);
            if (!files.mkdir()) {
                throw new VCSException("Cannot initialize files directory");
            }

            File stage = new File(VCS.STAGE_DIRECTORY);
            if (!stage.mkdir()) {
                throw new VCSException("Cannot initialize stage directory");
            }

            File delete = new File(VCS.DELETE_DIRECTORY);
            if (!delete.mkdir()) {
                throw new VCSException("Cannot initialize delete directory");
            }

            db.connect();
            db.initTables();
            db.addBranch("master");
            db.switchBranch("master");

        } catch (ClassNotFoundException e) {
            throw new VCSException("Cannot find SQLite", e);
        } catch (SQLException e) {
            throw new VCSException("SQL exception:" + e.getMessage(), e);
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
                File[] filesInDirectory = currentDirectory.listFiles(weakFilter);
                if (filesInDirectory != null) {
                    for (File file : filesInDirectory) {
                        if (file.isFile()) {
                            file.delete();
                        } else {
                            FileUtils.deleteDirectory(file);
                        }
                    }
                }

                for (VCSEntity entity : commitFiles) {
                    FileUtils.copyFile(new File(VCS.FILES_DIRECTORY + "/" + entity.fileId), new File(entity.path));
                }
            }
            db.switchBranch(branchName);
        } catch (ClassNotFoundException e) {
            throw new VCSException("Cannot find SQLite", e);
        } catch (SQLException e) {
            throw new VCSException("SQL exception:" + e.getMessage(), e);
        } catch (IOException e) {
            throw new VCSException("Cannot switch files during checkout:" + e.getMessage(), e);
        }
    }

    public void merge(String sourceBranch) {
        try {
            db.connect();
            CommitResult commitData = db.getLastCommit(sourceBranch);

            Map<String, Integer> commitFiles = db.commitFiles(commitData).stream()
                    .collect(Collectors.toMap((it) -> it.path, (it) -> it.fileId));

            startMerge(new File("."), commitFiles);
        } catch (ClassNotFoundException e) {
            throw new VCSException("Cannot find SQLite", e);
        } catch (SQLException e) {
            throw new VCSException("SQL exception:" + e.getMessage(), e);
        } catch (IOException e) {
            throw new VCSException("Cannot switch files during merge:" + e.getMessage(), e);
        }
    }

    public void branch(String branchName, ModifyAction action) {
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
                    break;
            }
        } catch (ClassNotFoundException e) {
            throw new VCSException("Cannot find SQLite", e);
        } catch (SQLException e) {
            throw new VCSException("SQL exception:" + e.getMessage(), e);
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
            Set<VCSEntity> deletedFiles = getDirectoryFiles(new File(VCS.DELETE_DIRECTORY), VCS.DELETE_DIRECTORY).stream()
                    .map((it) -> new VCSEntity(0, it)).collect(Collectors.toSet());
            CommitResult commitData = db.commit(message);
            File stage = new File(VCS.STAGE_DIRECTORY);
            Set<VCSEntity> stageEntities = registerDirectory(stage, ".");
            stageEntities.removeAll(deletedFiles);

            for (VCSEntity entity : stageEntities) {
                db.addFileToCommit(commitData.commitId, entity.fileId);
                lastCommitFiles.remove(entity);
            }

            for (VCSEntity entity : lastCommitFiles) {
                db.addFileToCommit(commitData.commitId, entity.fileId);
            }

            FileUtils.deleteDirectory(new File(VCS.STAGE_DIRECTORY));
            FileUtils.forceMkdir(new File(VCS.STAGE_DIRECTORY));
            FileUtils.deleteDirectory(new File(VCS.DELETE_DIRECTORY));
            FileUtils.forceMkdir(new File(VCS.DELETE_DIRECTORY));
        } catch (ClassNotFoundException e) {
            throw new VCSException("Cannot find SQLite", e);
        } catch (SQLException e) {
            throw new VCSException("SQL exception:" + e.getMessage(), e);
        } catch (IOException e) {
            throw new VCSException("Cannot copy commit files:" + e.getMessage(), e);
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
            throw new VCSException("Cannot find SQLite", e);
        } catch (SQLException e) {
            throw new VCSException("SQL exception:" + e.getMessage(), e);
        }
    }

    public void status() {
        try {
            db.connect();
            String currentBranch = db.getCurrentBranch();
            System.out.println("On branch " + currentBranch + ":");

            List<String> stageFiles = getDirectoryFiles(new File(VCS.STAGE_DIRECTORY), VCS.STAGE_DIRECTORY);

            if (!stageFiles.isEmpty()) {
                System.out.println("Changes to be committed:");
                stageFiles.stream().sorted().forEach(System.out::println);
            }

            List<String> deletedFiles = getDirectoryFiles(new File(VCS.DELETE_DIRECTORY), VCS.DELETE_DIRECTORY);
            if (!deletedFiles.isEmpty()) {
                System.out.println("Files will be deleted:");
                deletedFiles.stream().sorted().forEach(System.out::println);
            }

            CommitResult lastCommit = null;
            if (currentBranch != null) {
                lastCommit = db.getLastCommit(currentBranch);
            }

            Map<String, Integer> lastCommitFiles = db.commitFiles(lastCommit).stream()
                    .collect(Collectors.toMap((x) -> x.path, (y) -> y.fileId));
            List<VCSEntity> allFiles = getDirectoryFiles(new File("."), ".", strongFilter).stream()
                    .map((it) -> new VCSEntity(-1, it)).collect(Collectors.toList());


            List<VCSFile> notStagedChanges = new ArrayList<>();
            for (VCSEntity entity : allFiles) {
                File compareTarget = null;
                File stageFile = new File(VCS.STAGE_DIRECTORY + "/" + entity.path);
                if (stageFile.exists()) {
                    compareTarget = stageFile;
                } else if (lastCommitFiles.containsKey(entity.path)) {
                    compareTarget = new File(VCS.FILES_DIRECTORY + "/" + lastCommitFiles.get(entity.path));
                }

                if (compareTarget == null) {
                    notStagedChanges.add(new VCSFile(entity, ModifyAction.NEW));
                } else if (!isFilesEquals(new File("./" + entity.path), compareTarget)) {
                    notStagedChanges.add(new VCSFile(entity, ModifyAction.MODIFIED));
                }
            }

            if (!notStagedChanges.isEmpty()) {
                System.out.println("Changes not staged for commit:");
                notStagedChanges.stream()
                        .sorted(Comparator.comparing(e -> e.entity.path))
                        .forEach((it) -> System.out.println(it.action.name() + " " + it.entity.path));
            }
        } catch (ClassNotFoundException e) {
            throw new VCSException("Cannot find SQLite", e);
        } catch (SQLException e) {
            throw new VCSException("SQL exception:" + e.getMessage(), e);
        } catch (IOException e) {
            throw new VCSException("Cannot evaluate IO operation:" + e.getMessage(), e);
        }
    }

    public void clean() {
        try {
            db.connect();

            FileUtils.deleteDirectory(new File(VCS.STAGE_DIRECTORY));
            FileUtils.forceMkdir(new File(VCS.STAGE_DIRECTORY));
            FileUtils.deleteDirectory(new File(VCS.DELETE_DIRECTORY));
            FileUtils.forceMkdir(new File(VCS.DELETE_DIRECTORY));

            String currentBranch = db.getCurrentBranch();
            CommitResult lastCommit = null;
            if (currentBranch != null) {
                lastCommit = db.getLastCommit(currentBranch);
            }
            Set<String> lastCommitFiles = db.commitFiles(lastCommit).stream()
                    .map(it -> it.path).collect(Collectors.toSet());
            List<VCSEntity> allFiles = getDirectoryFiles(new File("."), ".", strongFilter).stream()
                    .map((it) -> new VCSEntity(-1, it)).collect(Collectors.toList());

            for (VCSEntity entity : allFiles) {
                if (!lastCommitFiles.contains(entity.path)) {
                    new File(entity.path).delete();
                }
            }

        } catch (ClassNotFoundException e) {
            throw new VCSException("Cannot find SQLite", e);
        } catch (SQLException e) {
            throw new VCSException("SQL exception:" + e.getMessage(), e);
        } catch (IOException e) {
            throw new VCSException("Cannot evaluate IO operation:" + e.getMessage(), e);
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
                FileUtils.copyFile(source, new File(VCS.STAGE_DIRECTORY + "/" + path));
            }
        } catch (ClassNotFoundException e) {
            throw new VCSException("Cannot find SQLite", e);
        } catch (SQLException e) {
            throw new VCSException("SQL exception:" + e.getMessage(), e);
        } catch (IOException e) {
            throw new VCSException("Cannot copy file to stage", e);
        }
    }

    public void resetFileInStage(String path) {
        try {
            File deletedFile = new File(VCS.DELETE_DIRECTORY + "/" + path);
            File stageFile = new File(VCS.STAGE_DIRECTORY + "/" + path);
            File targetFile = new File("./" + path);
            if (deletedFile.exists()) {
                targetFile.delete();
                FileUtils.moveFile(deletedFile, new File("./" + path));
            } else if (stageFile.exists()) {
                targetFile.delete();
                FileUtils.moveFile(stageFile, new File("./" + path));
            } else {
                throw new VCSException("Cannot found file " + path);
            }
        } catch (IOException e) {
            throw new VCSException("Cannot move file from stage", e);
        }
    }

    public void removeFileInStage(String path) {
        path = resolveFilePath(path);
        File source = new File("./" + path);
        if (!source.exists()) {
            throw new VCSException("file " + path + " not found");
        }

        try {
            FileUtils.moveFile(source, new File(VCS.DELETE_DIRECTORY + "/" + path));
        } catch (IOException e) {
            throw new VCSException("Cannot delete file to stage", e);
        }
    }

    private Set<VCSEntity> registerDirectory(File sourceDirectory, String prefix) throws SQLException, IOException {
        Set<VCSEntity> hs = new HashSet<>();
        File[] filesInSourceDirectory = sourceDirectory.listFiles(weakFilter);
        if (filesInSourceDirectory != null) {
            for (File file : filesInSourceDirectory) {
                String stageFilePath = prefix + "/" + file.getName();
                if (file.isDirectory()) {
                    hs.addAll(registerDirectory(file, stageFilePath));
                } else {
                    String resolvedPath = resolveFilePath(stageFilePath);
                    int fileId = db.registerFile(resolvedPath);
                    FileUtils.copyFile(file, new File(VCS.FILES_DIRECTORY + "/" + fileId));
                    hs.add(new VCSEntity(fileId, resolvedPath));
                }
            }
        }
        return hs;
    }

    private void startMerge(File target, Map<String, Integer> source) throws IOException {
        mergeDirectory(target, source);
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            Path directoryPath = Paths.get(entry.getKey()).getParent();
            FileUtils.forceMkdir(new File(directoryPath.toString()));

            FileUtils.copyFile(new File(VCS.FILES_DIRECTORY + "/" + entry.getValue()), new File(entry.getKey()));
        }
    }

    private void mergeDirectory(File target, Map<String, Integer> source) throws IOException {
        File[] filesInTargetDirectory = target.listFiles(weakFilter);
        if (filesInTargetDirectory != null) {
            for (File file : filesInTargetDirectory) {
                String filePath = resolveFilePath(file.getAbsolutePath());
                if (source.containsKey(filePath)) {
                    if (file.isDirectory()) {
                        mergeDirectory(file, source);
                    } else {
                        int fileId = source.get(filePath);
                        mergeFiles(file, new File(VCS.FILES_DIRECTORY + "/" + fileId));
                        source.remove(filePath);
                    }
                }
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
                    resultLines.append("+").append(targetLines.get(i)).append("\n");
                }
                for (int i = startPointer; i < pointer; ++i) {
                    resultLines.append("-").append(sourceLines.get(i)).append("\n");
                }
            }
            if (pointer < lastLine) {
                resultLines.append(targetLines.get(pointer)).append("\n");
            }
        }

        for (int pointer = lastLine; pointer < targetLines.size(); ++pointer) {
            resultLines.append('+').append(targetLines.get(pointer)).append('\n');
        }

        for (int pointer = lastLine; pointer < sourceLines.size(); ++pointer) {
            resultLines.append('-').append(sourceLines.get(pointer)).append('\n');
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
        return !isFilesEquals(new File("./" + path), new File(VCS.FILES_DIRECTORY + "/" + fileId));
    }
}
