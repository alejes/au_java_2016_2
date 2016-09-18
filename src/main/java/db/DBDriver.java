package db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public interface DBDriver {
    void connect() throws ClassNotFoundException, SQLException;

    void initTables() throws SQLException;

    void addBranch(String branchName) throws SQLException;

}
