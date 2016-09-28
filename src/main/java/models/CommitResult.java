package models;

public class CommitResult {
    public int branchId;
    public int commitId;

    public CommitResult(int branchId, int commitId) {
        this.branchId = branchId;
        this.commitId = commitId;
    }
}