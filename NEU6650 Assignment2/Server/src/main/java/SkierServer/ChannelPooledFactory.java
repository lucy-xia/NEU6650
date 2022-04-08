package SkierServer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Getter
public class ChannelPooledFactory extends BasePooledObjectFactory<Channel> {

    private static final String QUEUE_NAME = "test1",
                                HOST = "34.216.176.227",
                                USER_NAME = "root",
                                PASSWORD = "123456";

    private final ConnectionFactory factory;
    private final Connection connection;
    private final String queueName;

    public ChannelPooledFactory() throws IOException, TimeoutException {
        this.factory = new ConnectionFactory();
        this.queueName = QUEUE_NAME;
        this.factory.setHost(HOST);
        this.factory.setUsername(USER_NAME);
        this.factory.setPassword(PASSWORD);
        this.connection = factory.newConnection();
    }

    @Override
    public Channel create() throws Exception {
        final boolean DURABLE = true, EXCLUSIVE = false, AUTO_DELETE = false;
        Channel channel = this.connection.createChannel();
        return channel;
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<>(channel);
    }
}