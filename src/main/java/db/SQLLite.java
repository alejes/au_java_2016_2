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
        PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM 'branches' WHERE ('branches'.'name' = ?)");
        preparedStatement.setString(1, branchName);
        ResultSet result = preparedStatement.executeQuery();
        if (result.getInt(1) > 0) {
            throw new VCSException("Branch already exists");
        }
        preparedStatement = conn.prepareStatement("INSERT INTO 'branches' VALUES(NULL, ?)");
        preparedStatement.setString(1, branchName);
        preparedStatement.executeUpdate();

        Statement stmt = conn.createStatement();
        stmt.executeUpdate("UPDATE 'settings' SET value='?' WHERE ('name' = 'current_branch');");
    }
}
