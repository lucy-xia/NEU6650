package mq;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class test {

    public static void main(String[] args) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(512);
        JedisPool pool = new JedisPool(poolConfig, "35.89.57.201", 6379, 2000, "123456");
        Jedis jedis = pool.getResource();
        jedis.rpush("123", "123");
        jedis.close();

    }

}
