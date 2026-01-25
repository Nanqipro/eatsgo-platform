package com.sky.test;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
public class SpringDataRedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedisTemplate() {
        System.out.println(redisTemplate);
        // redis的五种数据类型
        ValueOperations valueOperations = redisTemplate.opsForValue();
        HashOperations hashOperations = redisTemplate.opsForHash();
        ListOperations listOperations = redisTemplate.opsForList();
        SetOperations setOperations = redisTemplate.opsForSet();
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();


    }

    /**
     * 操作字符串类型的数据
     */
    @Test
    public void testString() {
        // set get setex setnx
        redisTemplate.opsForValue().set("name","张三");
        System.out.println(redisTemplate.opsForValue().get("name"));
        redisTemplate.opsForValue().set("code",1234, 60, TimeUnit.SECONDS);
        System.out.println(redisTemplate.opsForValue().get("code"));
        redisTemplate.opsForValue().setIfAbsent("code", 1234);

    }

    /**
     * 操作hash类型数据
     */
    @Test
    public void testHash() {
        // hset hget hdel hkeys hvals
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.put("user:1","name","张三");
        hashOperations.put("user:1","age",18);
        String name = (String) hashOperations.get("user:1", "name");
        System.out.println(name);
        Set keys = hashOperations.keys("user:1");
        System.out.println(keys);

        List values = hashOperations.values("user:1");
        System.out.println(values);


        hashOperations.delete("user:1","name");

    }


}
