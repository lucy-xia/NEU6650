package Client2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class ClientExtrance2 {

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 0;
        int numSkiers = 0;
        int numLifts = 0;
        int numRuns = 0;
        String ip = "";
        String port = "";
        Results2 results = new Results2();
        List<RecordElement2> recordElement = new ArrayList<>(); //print part2 result
        List<RecordElement2> recordList = new ArrayList<>();

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
        CountDownLatch latchSum = new CountDownLatch((int) (numThreads / 4.0 + numThreads + numThreads * 0.1));

        int count = 0;
        // Phase 1
        System.out.println("start phase 1");
        int numP1Threads = (int) (numThreads / 4.0);
        int numP1Requests = (int) Math.round((numRuns * 0.2) * (numSkiers / numP1Threads));
        CountDownLatch latch1 = new CountDownLatch((int)Math.ceil(numP1Threads * 0.2)); //Math.ceil() 返回大于或等于一个给定数字的最小整数
        for (int i = 0; i < numP1Threads; i++) {
            int startSkiers = 1 + (i * (numSkiers / numP1Threads));
            int endSkiers = (i + 1) * (numSkiers / numP1Threads);
            SkierThread2 thread1 = new SkierThread2(5, "2019", "7", startSkiers, endSkiers,
                1, 90, numLifts, numP1Requests, latch1, latchSum, results, recordElement, ip, port);
            thread1.start();
            recordList.addAll(thread1.getRecord());
        }
        latch1.await();

        // Phase 2
        System.out.println("start phase 2");
        int numP2Threads = numThreads;
        int numP2Requests = (int) Math.round((numRuns * 0.6) * (numSkiers / numP2Threads));
        CountDownLatch latch2 = new CountDownLatch((int)Math.ceil(numP2Threads * 0.2));
        for (int i = 0; i < numP2Threads; i++) {
            int startSkiers = 1 + (i * (numSkiers / numP2Threads));
            int endSkiers = (i + 1) * (numSkiers / numP2Threads);
            SkierThread2 thread2 = new SkierThread2(5, "2019", "7", startSkiers, endSkiers,
                91, 360, numLifts, numP2Requests, latch2, latchSum, results, recordElement, ip, port);
            thread2.start();
            recordList.addAll(thread2.getRecord());
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
            SkierThread2 thread3 = new SkierThread2(5, "2019", "7", startSkiers, endSkiers,
                361, 420, numLifts, numP3Requests, latch3, latchSum, results, recordElement, ip, port);
            thread3.start();
            recordList.addAll(thread3.getRecord());
        }
        latchSum.await();

        //Get timestamp at ending point
        DescriptiveStatistics stats = new DescriptiveStatistics();
        List<Long> latencies = new ArrayList<>();
        for (RecordElement2 record: recordList){
            stats.addValue(record.getLatency());
            latencies.add(record.getLatency());
        }
        System.out.println("-----------------------------------------------");
        System.out.println("Thread number: " + numThreads);
        System.out.println("Mean response time: " + stats.getMean() + " milliseconds");
        System.out.println("Median response time: " + stats.getPercentile(50) + " milliseconds");
        System.out.println("P99: " + stats.getPercentile(99) + " milliseconds");
        System.out.println("Min: " + stats.getMin() + " milliseconds; Max: " + stats.getMax() + " milliseconds");
        Collections.sort(latencies);

        //sout print out
        System.out.println("---------------------------------------------------");

    }
}
