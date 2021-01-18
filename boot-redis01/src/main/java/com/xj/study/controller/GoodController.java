package com.xj.study.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public String buy_Goods() {
        String value = UUID.randomUUID().toString()+Thread.currentThread().getName();
                try {
                    Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(REDIS_LOCK,value).booleanValue();
                    if(!flag){
                return "加锁失败！";

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
            stringRedisTemplate.delete(REDIS_LOCK);
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
