package db;

import exceptions.VCSException;
import models.CommitResult;
import models.VCSEntity;
import org.apache.commons.io.monitor.FileEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SQLLite implements DBDriver {
    Connection conn;

    @Override
    public void connect() throws ClassNotFoundException {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:./.vcs/vcs.s3db");
        } catch (SQLException ex) {
            throw new VCSException("database is corrupted", ex);
        }
    }

    @Override
    public void initTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS `branches` ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT);");
        stmt.execute("CREATE TABLE IF NOT EXISTS `commits` ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'branch' INTEGER, 'message' TEXT, 'date' TIMESTAMP);");
        stmt.execute("CREATE TABLE IF NOT EXISTS `settings` ('name' TEXT PRIMARY KEY , 'value' TEXT);");
        stmt.execute("CREATE TABLE IF NOT EXISTS `files` ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT);");
        stmt.execute("CREATE TABLE IF NOT EXISTS `commit_files` ('commit_id' INTEGER , 'file_id' INTEGER, PRIMARY KEY (`commit_id`, `file_id`));");
        stmt.execute("INSERT INTO `settings` VALUES ('current_branch', 'master');");
    }

    @Override
    public void addBranch(String branchName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM `branches` WHERE (`branches`.`name` = ?)");
        stmt.setString(1, branchName);
        ResultSet result = stmt.executeQuery();
        if (result.getInt(1) > 0) {
            throw new VCSException("Branch already exists");
        }
        stmt = conn.prepareStatement("INSERT INTO `branches` VALUES(NULL, ?)");
        stmt.setString(1, branchName);
        stmt.executeUpdate();
    }

    @Override
    public CommitResult getCommitById(String commit) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT `id`, `branch` FROM  `commits` WHERE (`commits`.`id` = ?);");
        stmt.setString(1, commit);
        ResultSet result = stmt.executeQuery();
        if (result.isClosed()) {
            return null;
        }
        int commitId = result.getInt(1);
        int branchId = result.getInt(2);
        return new CommitResult(branchId, commitId);
    }

    @Override
    public CommitResult getLastCommit(String branch) throws SQLException {
        Integer branchId = getBranchId(branch);
        if (branchId == null) {
            return null;
        }
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery("SELECT `id` FROM  `commits` WHERE (`commits`.`branch` = '" + branchId + "') ORDER by `commits`.`id` DESC;");
        if (result.isClosed()) {
            return null;
        }
        int last_commit_id = result.getInt(1);
        return new CommitResult(branchId, last_commit_id);
    }

    @Override
    public void addFileToCommit(int commitId, int fileId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO `commit_files` VALUES(? ,?)");
        stmt.setInt(1, commitId);
        stmt.setInt(2, fileId);
        stmt.executeUpdate();
    }

    @Override
    public void switchBranch(String branchName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE `settings` SET `value`=? WHERE (`settings`.`name` = 'current_branch');");
        stmt.setString(1, branchName);
        stmt.executeUpdate();
    }

    @Override
    public Set<VCSEntity> commitFiles(CommitResult commit) throws SQLException {
        Set<VCSEntity> result = new HashSet<>();
        if (commit == null){
            return result;
        }

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT `commit_files`.`file_id`, `files`.`name` " +
                        "FROM `commit_files` " +
                        "LEFT JOIN `files` ON `files`.`id`=`commit_files`.`file_id` " +
                        "WHERE (`commit_files`.`commit_id` = ?);");
        stmt.setInt(1, commit.commitId);
        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            int fileId = resultSet.getInt(1);
            String path = resultSet.getString(2);
            result.add(new VCSEntity(fileId, path));
        }
        return result;
    }

    @Override
    public Integer getFileIdInCommit(int commitId, String path) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT `commit_files`.`file_id` " +
                        "FROM `commit_files` " +
                        "LEFT JOIN `files` ON `files`.`id`=`commit_files`.`file_id` " +
                        "WHERE ((`commit_files`.`commit_id` = ?) and (`files`.`name` = ?));");
        stmt.setInt(1, commitId);
        stmt.setString(2, path);
        System.out.println("SELECT `commit_files`.`file_id` " +
                "FROM `commit_files` " +
                "LEFT JOIN `files` ON `files`.`id`=`commit_files`.`file_id` " +
                "WHERE ((`commit_files`.`commit_id` = "+commitId+") and (`files`.`name` = '"+path+"'));");
        ResultSet resultSet = stmt.executeQuery();

        if (resultSet.isClosed()) {
            return null;
        }

        return resultSet.getInt(1);
    }

    @Override
    public void deleteBranch(String branchName) throws SQLException {
        if (getCurrentBranch().equals(branchName)) {
            throw new VCSException("You cannot delete current branch");
        }
        PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM `branches` WHERE (`branches`.`name` = ?);");
        preparedStatement.setString(1, branchName);
        preparedStatement.executeUpdate();
    }

    @Override
    public CommitResult commit(String message) throws SQLException {
        String branchName = getCurrentBranch();
        int branchId = getBranchId(branchName);

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO `commits` VALUES(NULL, ?, ?, CURRENT_TIMESTAMP)");
        stmt.setInt(1, branchId);
        stmt.setString(2, message);
        stmt.executeUpdate();

        ResultSet result = stmt.getGeneratedKeys();
        int insert_id = result.getInt(1);

        return new CommitResult(branchId, insert_id);
    }

    @Override
    public int registerFile(String path) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO `files` VALUES(NULL, ?)");
        stmt.setString(1, path);
        stmt.executeUpdate();

        ResultSet result = stmt.getGeneratedKeys();
        int insert_id = result.getInt(1);

        return insert_id;
    }

    @Override
    public String log() throws SQLException {
        String branchName = getCurrentBranch();
        Integer branchId = getBranchId(branchName);
        if (branchId == null) {
            throw new VCSException("Illegal State: wrong current branch name");
        }
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `commits` WHERE (`commits`.`branch`=?) ORDER BY `commits`.`id` DESC LIMIT 7");
        stmt.setInt(1, branchId);

        ResultSet result = stmt.executeQuery();
        StringBuilder answer = new StringBuilder();
        while (result.next()) {
            int commitId = result.getInt(1);
            String message = result.getString(3);
            String time = result.getString(4);
            answer.append(commitId + ": " + time + "\n");
            answer.append(message);
            answer.append("\n=====\n");
        }
        result.close();

        return answer.toString();
    }

    @Override
    public String getCurrentBranch() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery("SELECT `value` FROM  `settings` WHERE (`settings`.`name` = 'current_branch');");
        return result.getString(1);
    }

    @Override
    public Integer getBranchId(String branchName) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement("SELECT `id` FROM `branches` WHERE (`branches`.`name` = ?);");
        preparedStatement.setString(1, branchName);
        ResultSet result = preparedStatement.executeQuery();
        if (result.isClosed()) {
            return null;
        } else {
            return result.getInt(1);
        }
    }
}
