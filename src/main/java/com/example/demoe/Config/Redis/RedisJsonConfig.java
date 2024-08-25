//package com.example.demoe.Config.Redis;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import redis.clients.jedis.Jedis;
//
//@Configuration
//public class RedisJsonConfig {
//
//    @Bean
//    public Jedis jedis() {
//        // Kết nối đến Redis server
//        return new Jedis("localhost", 6380); // Cổng RedisJSON
//    }
//
//    @Bean
//    public ObjectMapper objectMapper() {
//        return new ObjectMapper();
//    }
//}
