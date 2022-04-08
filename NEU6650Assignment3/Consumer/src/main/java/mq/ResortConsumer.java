
package mq;
    import lombok.SneakyThrows;
    import redis.clients.jedis.Jedis;
    import com.google.gson.Gson;
    import com.google.gson.GsonBuilder;
    import com.google.gson.JsonObject;
    import com.rabbitmq.client.Channel;
    import com.rabbitmq.client.Connection;
    import com.rabbitmq.client.ConnectionFactory;
    import com.rabbitmq.client.Consumer;
    import com.rabbitmq.client.DeliverCallback;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.List;
    import redis.clients.jedis.JedisPool;
    import redis.clients.jedis.JedisPoolConfig;

public class ResortConsumer {

    //private final static String QUEUE_NAME = "QUEUE_NAME_SKIER";
    private final static String QUEUE_NAME = "test2";
    private final static Integer NUM_THREADS = 32;

    public static void main(String[] args) throws Exception {
        //test JedisPool connection
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(512);
        JedisPool pool = new JedisPool(poolConfig, "35.89.57.201", 6379, 2000, "123456");
        System.out.println("Redis connected successfully");

        //rabbitmq connection
        Gson gson  = new Gson();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("34.209.28.194");
        factory.setPort(5672);
        factory.setUsername("root");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        System.out.println("rabbitmq connected successfully");

        Runnable runnable = new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
//                try () {
                Jedis jedis = pool.getResource();
                final Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                channel.basicQos(1);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    try{
                        String message = new String(delivery.getBody(), "UTF-8");
                        JsonObject json = gson.fromJson(message, JsonObject.class);
                        String key = String.valueOf(json.get("dayID"));
                        String skierId = String.valueOf(json.get("skierId"));
                        String liftId = String.valueOf(json.get("liftId"));
                        String resortId = String.valueOf(json.get("resortID"));
                        String info = resortId + "," + skierId + ","  + liftId;
                        System.out.println(key);
                        System.out.println(info);
                        //save into list repectively, key is the name of the list
                        jedis.rpush(key, info);
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                };
                // process messages
                channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
//                } catch (IOException ex) {
//
//                }
            }
        };

        List<Thread> listThread = new ArrayList<>();
        for(int i = 0; i < NUM_THREADS; i++){
            Thread cons = new Thread(runnable);
            cons.start();
            listThread.add(cons);
        }
        for (int i = 0; i < NUM_THREADS; i++){
            listThread.get(i).join();
        }
        pool.close();
    }
}