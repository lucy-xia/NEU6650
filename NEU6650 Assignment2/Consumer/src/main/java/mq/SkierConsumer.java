package mq;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;


public class SkierConsumer {
    private final static String QUEUE_NAME = "test1";
    private final static Integer NUM_THREADS = 32;

    public static void main(String[] args) throws Exception {
        System.out.println("Start");
        Gson gson  = new Gson();
        ConnectionFactory factory = new ConnectionFactory();
        ConcurrentHashMap<Integer, List<JsonObject>> map = new ConcurrentHashMap<>();

        factory.setHost("34.216.176.227");
        factory.setPort(5672);
        factory.setUsername("root");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final
                    Channel channel = connection.createChannel();
                    channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                    // max one message per receiver
                    channel.basicQos(1);


                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), "UTF-8");
                        System.out.println(message);
                        JsonObject json = gson.fromJson(message, JsonObject.class);
                        Integer key = Integer.valueOf(String.valueOf(json.get("id")));
                        if(map.containsKey(key)){
                            map.get(key).add(json);
                        } else{
                            List<JsonObject> value = new ArrayList<>();
                            value.add(json);
                            map.put(key, value);

                        }
                         channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                    };
                    // process messages
                    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
                } catch (IOException ex) {
                    Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        for (int i = 0; i < NUM_THREADS; i ++){
            Thread cons = new Thread(runnable);
            cons.start();
        }

    }
}
