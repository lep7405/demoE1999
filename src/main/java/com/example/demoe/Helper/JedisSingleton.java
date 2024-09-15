package com.example.demoe.Helper;
import redis.clients.jedis.UnifiedJedis;

public class JedisSingleton {
    private static UnifiedJedis instance;

    // private constructor to prevent instantiation
    private JedisSingleton() {}

    public static UnifiedJedis getInstance() {
        if (instance == null) {
            synchronized (JedisSingleton.class) {
                if (instance == null) {
                    instance = new UnifiedJedis("redis://localhost:6380");
                }
            }
        }
        return instance;
    }
}
