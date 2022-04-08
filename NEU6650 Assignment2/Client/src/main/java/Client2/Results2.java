package Client2;

public class Results2 {

    private volatile int successfulPosts;
    private int failedPosts;

    public Results2() {
        this.successfulPosts = 0;
        this.failedPosts = 0;
    }

    public synchronized void incrementSuccessfulPost(int increment) {
        this.successfulPosts += increment;
    }

    public synchronized void incrementFailedPost(int increment) {
        this.failedPosts += increment;
    }

    public int getSuccessfulPosts() {
        return this.successfulPosts;
    }

    public int getFailedPosts() {
        return this.failedPosts;
    }
}