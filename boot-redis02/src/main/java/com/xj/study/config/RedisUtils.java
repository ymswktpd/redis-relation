package com.xj.study.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author xijie
 * @version V1.0
 * Date 2021/1/19 0:20
 * @Description:
 */
public class RedisUtils {

    private static JedisPool jedisPool;
    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        jedisPool = new JedisPool(jedisPoolConfig,"192.168.0.3",6379);
    }

    public static Jedis getJedis() throws Exception {
        if(null !=jedisPool){
            return jedisPool.getResource();
        }
        throw  new Exception("Jedispool is not ok");
    }

}
