package db;

import models.CommitResult;
import java.sql.SQLException;

public interface DBDriver {
    void connect() throws ClassNotFoundException;

    void initTables() throws SQLException;

    void addBranch(String branchName) throws SQLException;

    void switchBranch(String branchName) throws SQLException;

    void deleteBranch(String branchName) throws SQLException;

    CommitResult commit(String message) throws SQLException;

    String log() throws SQLException;

    CommitResult getLastCommit(String branch) throws SQLException;

    CommitResult getCommitById(String commitId) throws SQLException;

    String getCurrentBranch() throws SQLException;

    int registerFile(String path) throws SQLException;

    void addFileToCommit(int commitId, int fileId) throws SQLException;

    Integer getBranchId(String branchName) throws SQLException;

}
