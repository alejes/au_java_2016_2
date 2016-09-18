package db;


import exceptions.VCSException;

import java.sql.*;

public class SQLLite implements DBDriver {
    Connection conn;
    ResultSet resSet;

    @Override
    public void connect() throws ClassNotFoundException, SQLException {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:./.vcs/vcs.s3db");

    }

    @Override
    public void initTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS 'branches' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT);");
        stmt.execute("CREATE TABLE IF NOT EXISTS 'commits' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'branch' INTEGER, 'message' TEXT, 'date' TIMESTAMP);");
        stmt.execute("CREATE TABLE IF NOT EXISTS 'settings' ('name' TEXT PRIMARY KEY , 'value' TEXT);");
        stmt.execute("INSERT INTO 'settings' VALUES ('current_branch', 'master');");
    }

    @Override
    public void addBranch(String branchName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM 'branches' WHERE ('branches'.'name' = ?)");
        stmt.setString(1, branchName);
        ResultSet result = stmt.executeQuery();
        if (result.getInt(1) > 0) {
            throw new VCSException("Branch already exists");
        }
        stmt = conn.prepareStatement("INSERT INTO 'branches' VALUES(NULL, ?)");
        stmt.setString(1, branchName);
        stmt.executeUpdate();
    }

    @Override
    public void switchBranch(String branchName) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("UPDATE 'settings' SET value='?' WHERE ('name' = 'current_branch');");
    }

    @Override
    public void deleteBranch(String branchName) throws SQLException {
        if (getCurrentBranch().equals(branchName)) {
            throw new VCSException("You cannot delete current branch");
        }
        PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM 'settings' WHERE ('name' = ?);");
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

    private String getCurrentBranch() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery("SELECT `value` FROM  `settings` WHERE ('name' = 'current_branch');");
        return result.getString(1);
    }

    private int getBranchId(String branchName) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement("SELECT `id` FROM `branches` WHERE ('name' = ?);");
        preparedStatement.setString(1, branchName);
        ResultSet result = preparedStatement.executeQuery();
        return result.getInt(1);
    }
}
