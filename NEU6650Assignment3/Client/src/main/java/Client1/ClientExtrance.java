package Client1;

import Client2.Record2;
import java.util.concurrent.CountDownLatch;

public class ClientExtrance {
    private static Integer SINGLE_THROUGHPUT = 53;

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 0;
        int numSkiers = 0;
        int numLifts = 0;
        int numRuns = 0;
        String ip = "";
        String port = "";
        Results results = new Results();

        //Get input parameters
        for (int i  = 0; i < args.length; i++) {
            numThreads = Integer.parseInt(args[0]);
            numSkiers = Integer.parseInt(args[1]);
            numLifts = Integer.parseInt(args[2]);
            numRuns = Integer.parseInt(args[3]);
            ip = args[4];
            port = args[5];
        }

        //Get timestamp at starting point
        long startTime = System.currentTimeMillis();


        // count the sum of three phases
        CountDownLatch latchSum = new CountDownLatch(
            (int) (numThreads / 4.0 + numThreads + numThreads * 0.1));


        // Phase 1
        System.out.println("start phase 1");
        int numP1Threads = (int) (numThreads / 4.0);
        int numP1Requests = (int) Math.round((numRuns * 0.2) * (numSkiers / numP1Threads));
        CountDownLatch latch1 = new CountDownLatch((int)Math.ceil(numP1Threads * 0.2)); //Math.ceil() 返回大于或等于一个给定数字的最小整数

        for (int i = 0; i < numP1Threads; i++) {
            int startSkiers = 1 + (i * (numSkiers / numP1Threads));
            int endSkiers = (i + 1) * (numSkiers / numP1Threads);
            SkierThread thread = new SkierThread(5, "2019", "7", startSkiers, endSkiers,
                1, 90, numLifts, numP1Requests, latch1, latchSum, results, ip, port);
            thread.start();
        }
        latch1.await();

        // Phase 2
        System.out.println("start phase 2");
        int numP2Threads = numThreads;
        int numP2Requests = (int) Math.round((numRuns * 0.6) * (numSkiers / numThreads));
        CountDownLatch latch2 = new CountDownLatch((int)Math.ceil(numThreads * 0.2));
        for (int i = 0; i < numP2Threads; i++) {
            int startSkiers = 1 + (i * (numSkiers / numP2Threads));
            int endSkiers = (i + 1) * (numSkiers / numP2Threads);
            SkierThread thread = new SkierThread(5, "2019", "7", startSkiers, endSkiers,
                91, 360, numLifts, numP2Requests, latch2, latchSum, results, ip, port);
            thread.start();
        }
        latch2.await();


        // Phase 3
        System.out.println("start phase 3");
        int numP3Threads = (int) (numThreads * 0.1);
        int numP3Requests = (int) Math.round(numRuns * 0.1);
        CountDownLatch latch3 = new CountDownLatch((int)Math.ceil(numThreads * 0.2));
        for (int i = 0; i < numP2Threads; i++) {
            int startSkiers = 1 + (i * (numSkiers / numP3Threads));
            int endSkiers = (i + 1) * (numSkiers / numP3Threads);
            SkierThread thread = new SkierThread(5, "2019", "7", startSkiers, endSkiers,
                361, 420, numLifts, numP3Requests, latch3, latchSum, results, ip, port);
            thread.start();
        }
        latchSum.await();

        //Get timestamp at ending point
        long endTime = System.currentTimeMillis();
        long wallTime = endTime - startTime;
        int success = results.getSuccessfulPosts();
        int failed = results.getFailedPosts();
        long throughPut = 1000 * (success + failed) / wallTime;
        long predictedMaxThroughput = (numP1Requests*numP1Threads + numP2Requests*numThreads + numP3Requests*numP3Threads)/
            (numP1Requests+numP2Requests+numP3Requests)* SINGLE_THROUGHPUT;
        int predictedMinThroughput = (int) Math.round(SINGLE_THROUGHPUT * numThreads * 0.1);

        //sout print out
        System.out.println("---------------------------------------------------");
        System.out.println("Successful request sent" + success);
        System.out.println("Unsuccessful request sent" + failed);
        System.out.println("Total run time (wall time)" + wallTime);
        System.out.println("ThroughPut" + throughPut);

        System.out.println("Thread number:" + numThreads);
        System.out.println("numSkiers " + 20000);
        System.out.println("numLifts " + 40);
        System.out.println("throughPurt" + throughPut);
        System.out.println("The total run time(wall time): " + wallTime + " milliseconds");


    }
}
