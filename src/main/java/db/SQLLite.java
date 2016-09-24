package db;

import exceptions.VCSException;

import java.sql.*;

public class SQLLite implements DBDriver {
    Connection conn;

    @Override
    public void connect() throws ClassNotFoundException, SQLException {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:./.vcs/vcs.s3db");

    }

    @Override
    public void initTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS `branches` ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT);");
        stmt.execute("CREATE TABLE IF NOT EXISTS `commits` ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'branch' INTEGER, 'message' TEXT, 'date' TIMESTAMP);");
        stmt.execute("CREATE TABLE IF NOT EXISTS `settings` ('name' TEXT PRIMARY KEY , 'value' TEXT);");
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
    public void switchBranch(String branchName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE `settings` SET `value`=? WHERE (`settings`.`name` = 'current_branch');");
        stmt.setString(1, branchName);
        stmt.executeUpdate();
    }

    @Override
    public void deleteBranch(String branchName) throws SQLException {
        if (getCurrentBranch().equals(branchName)) {
            throw new VCSException("You cannot delete current branch");
        }
        PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM `settings` WHERE (`settings`.`name` = ?);");
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

    private Integer getBranchId(String branchName) throws SQLException {
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
