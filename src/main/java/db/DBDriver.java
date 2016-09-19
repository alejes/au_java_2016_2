package db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public interface DBDriver {
    class CommitResult {
        public int branchId;
        public int commitId;

        public CommitResult(int branchId, int commitId) {
            this.branchId = branchId;
            this.commitId = commitId;
        }
    }

    void connect() throws ClassNotFoundException, SQLException;

    void initTables() throws SQLException;

    void addBranch(String branchName) throws SQLException;

    void switchBranch(String branchName) throws SQLException;

    void deleteBranch(String branchName) throws SQLException;

    CommitResult commit(String message) throws SQLException;

    String log() throws SQLException;

    CommitResult getLastCommit(String branch) throws SQLException;

    CommitResult getCommitById(String commitId) throws SQLException;
}
