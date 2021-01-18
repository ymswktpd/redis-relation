package com.xj.study.controller;

import com.xj.study.config.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xijie
 * @version V1.0
 * Date 2021/1/17 13:18
 * @Description:
 */
@RestController
public class GoodController {
    @Value("${server.port}")
    private String serverport;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private String REDIS_LOCK = "atguigulock";
//    private Lock lock = new ReentrantLock();

    @GetMapping("/buy_Goods")
    public String buy_Goods() throws Exception {
        String value = UUID.randomUUID().toString()+Thread.currentThread().getName();
        try {
            Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(REDIS_LOCK,value,10L,TimeUnit.SECONDS).booleanValue();
            if(!flag){
                return "获取锁失败！";

            }
            String goodsNum = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = goodsNum == null ? 0 : Integer.valueOf(goodsNum);
            if (goodsNumber > 0) {
                int realGoodsNum = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001", String.valueOf(realGoodsNum));
                System.out.println("成功买到商品，库存还剩下：" + realGoodsNum + " 件，" + "\t 服务提供端口" + serverport);
                return "成功买到商品，库存还剩下：" + realGoodsNum + " 件，" + "\t 服务提供端口" + serverport;
            } else {
                System.out.println("商品已售罄/活动结束/调用超时，欢迎下次光临！，" + "\t 服务提供端口" + serverport);
            }
            return "商品已售罄/活动结束/调用超时，欢迎下次光临！，" + "\t 服务提供端口" + serverport;
        }finally {
            //判断加锁与解锁不是原子性的，锁失效后会出现误解锁现象，需要调用lua脚本
//            if(stringRedisTemplate.opsForValue().get(REDIS_LOCK).equalsIgnoreCase(value)){
//                stringRedisTemplate.delete(REDIS_LOCK);
//            }
            //        不用lua脚本，如何处理误解锁（redis事务）
//            while (true){
//                stringRedisTemplate.watch(REDIS_LOCK);
//                if(stringRedisTemplate.opsForValue().get(REDIS_LOCK).equalsIgnoreCase(value)){
//                    //生产上不要随便设置
//                    stringRedisTemplate.setEnableTransactionSupport(true);
//                    stringRedisTemplate.multi();
//                    stringRedisTemplate.delete(REDIS_LOCK);
//                    List list = stringRedisTemplate.exec();
//                    if(list.size() == 0){
//                        continue;
//                    }
//                }
//                stringRedisTemplate.unwatch();
//                break;
//            }
//            使用lua脚本方式
                Jedis jedis = RedisUtils.getJedis();
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                        try{
                            Object o = jedis.eval(script, Collections.singletonList(REDIS_LOCK), Collections.singletonList(value));
                            if("1".equals(o.toString())){
                                System.out.println("-------del redis lock");
                            }else{
                                System.out.println("-------del redis lock failed");
                            }
                        }finally {
                    jedis.close();
                }
        }


    }
//        if(lock.tryLock(3L, TimeUnit.SECONDS)){
//            try {
//                lock.lock();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }finally {
//                lock.unlock();
//            }
//        }else{
//
//        }


}
