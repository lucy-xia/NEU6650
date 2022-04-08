package Client1;

import Client2.RecordElement2;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

/** Each thread should send (numRunsx0.2)x(numSkiers/(numThreads/4)) POST requests to the server
 *  Each POST should randomly select:
 * 1)a skierID from the range of ids passed to the thread
 * 2) a lift number (liftID)
 * 3) a time value from the range of minutes passed to each thread (between start and end time)
 * 4) a wait time between 0 and 10
 */
public class SkierThread extends Thread {

    private static int RETRY_TIMES = 5;

    private Integer resortID;
    private String seasonID;
    private String dayID;
    private Integer startSkierID;
    private Integer endSkierID;
    private Integer startTime;
    private Integer endTime;
    private Integer liftID;
    private int numPostRequests;
    private CountDownLatch latch;
    private CountDownLatch latchSum;
    private CountDownLatch curLatch;
    private Results results;
    private String ip;
    private String port;

    public SkierThread(Integer resortID, String seasonID, String dayID,
        Integer startSkierID, Integer endSkierID, Integer startTime, Integer endTime,
        Integer liftID, int numPostRequests, CountDownLatch latch, CountDownLatch latchSum,
        Results results, String ip, String port) {
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.startSkierID = startSkierID;
        this.endSkierID = endSkierID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.liftID = liftID;
        this.numPostRequests = numPostRequests;
        this.latch = latch;
        this.latchSum = latchSum;
        this.results = results;
        this.ip = ip;
        this.port = port;
    }


    @Override
    public void run() {
        HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).build();
        for (int i = 0; i < this.numPostRequests; i++) {
            GetMethod method = new GetMethod();
            // produce random post parameters
            int skierID = ThreadLocalRandom.current().nextInt(this.endSkierID - this.startSkierID) + this.startSkierID;
            int liftID = ThreadLocalRandom.current().nextInt(this.liftID) + 1;
            int timeRange = ThreadLocalRandom.current().nextInt(this.endTime - this.startTime) + this.startTime;
            int waitTime = ThreadLocalRandom.current().nextInt(10);
            long curStartPost = 0;
            long curEndPost = 0;
            try {
                long startPost = System.currentTimeMillis(); //time stamp before post sending
                curStartPost = startPost;

                //configure the post parameters
                String body = createBody(skierID, liftID, waitTime);
                String url = createUrl(resortID, seasonID, dayID, skierID, ip, port);//  /resortID/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
                method = new GetMethod(url);
                HttpPost httpPost = new HttpPost(url);

                // execute post
                StringBuilder sb = new StringBuilder();
                sb.append(body);
                HttpEntity responseContent = new StringEntity(sb.toString());
                httpPost.setEntity(responseContent);
                HttpResponse response = client.execute(httpPost);
                if(response.getStatusLine().getStatusCode() != 201){
                    RETRY_TIMES--;
                    while(response.getStatusLine().getStatusCode() != 201 && RETRY_TIMES > 0 ){
                        //configure the post parameters
                        body = createBody(skierID, liftID, waitTime);
                        url = createUrl(resortID, seasonID, dayID, skierID, ip, port);//  /resortID/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
                        method = new GetMethod(url);
                        httpPost = new HttpPost(url);

                        // execute post
                        sb = new StringBuilder();
                        sb.append(body);
                        responseContent = new StringEntity(sb.toString());
                        httpPost.setEntity(responseContent);
                        response = client.execute(httpPost);
                        RETRY_TIMES--;
                    }
                }
                EntityUtils.consume(response.getEntity());
                if(response.getStatusLine().getStatusCode() != 201){
                    this.results.incrementFailedPost(1);
                }
                this.results.incrementSuccessfulPost(1);

                //time stamp after post sending
                long endPost = System.currentTimeMillis();
                curEndPost = endPost;
                long latency = System.currentTimeMillis();

            } catch (IOException e) {
                this.results.incrementFailedPost(1);
                System.out.println(
                    "Exception occured, tried " + (5- RETRY_TIMES) + " times");
                e.printStackTrace();
            }
            //did not arraive here
        }
        try {
            latch.countDown();
            latchSum.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createBody(int a, int b, int c){
        String result = "{" +
                        "time:" + a + "," +
                        "liftID:" + b + "," +
                        "waitTime:" + c +
                        "}";
        return result;
    }

    public String createUrl(int a, String b, String c, int d, String ip, String port){
        String result = "http://" +
            ip + ":" +
            port + "/assignment1_1_war" + "/" +
            "skiers/" + a + "/" +
            "seasons/" + b + "/" +
            "days/" + c + "/" +
            "skiers/" + d;
        return result;
    }



}