package com.xj.study.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/buy_Goods")
    public String buy_Goods() {
        synchronized (this) {
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
        }
    }

}
