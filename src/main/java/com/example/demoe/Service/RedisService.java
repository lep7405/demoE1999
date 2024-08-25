//package com.example.demoe.Service;
//
//import org.springframework.stereotype.Service;
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.json.Path2;
//
//@Service
//public class RedisService {
//    private final Jedis jedis;
//
//    public RedisService(Jedis jedis) {
//        this.jedis = jedis;
//    }
//    public void saveCart(String redisKey, String json) {
//        // Sử dụng RedisJSON API từ Jedis
//        jedis.jsonSet(redisKey, Path2.ROOT_PATH, json);
//    }
//}
