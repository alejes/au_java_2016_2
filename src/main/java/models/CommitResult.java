package models;

public class CommitResult {
    public final int branchId;
    public final int commitId;

    public CommitResult(int branchId, int commitId) {
        this.branchId = branchId;
        this.commitId = commitId;
    }

    @Override
    public String toString() {
        return "CommitResult{" +
                "branchId=" + branchId +
                ", commitId=" + commitId +
                '}';
    }
}