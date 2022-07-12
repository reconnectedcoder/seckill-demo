package com.wang.seckill.rabbitmq;

import com.wang.seckill.pojo.SeckillMessage;
import com.wang.seckill.pojo.SeckillOrder;
import com.wang.seckill.pojo.User;
import com.wang.seckill.service.IGoodsService;
import com.wang.seckill.service.IOrderService;
import com.wang.seckill.utils.JsonUtil;
import com.wang.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {



//    @RabbitListener(queues = "queue")
//    public void receive(Object msg){
//        log.info("接收消息：" + msg);
//    }
//
//    /**
//     * 广播模式接收
//     * @param msg
//     */
//    @RabbitListener(queues = "queue_fanout01")
//    public void receive01(Object msg){
//        log.info("QUEUE01接收消息：" + msg);
//    }
//
//    @RabbitListener(queues = "queue_fanout02")
//    public void receive02(Object msg){
//        log.info("QUEUE02接收消息：" + msg);
//    }
//    /**
//     * 直连模式接收
//     */
//    @RabbitListener(queues = "queue_direct01")
//    public void receive03(Object msg){
//        log.info("QUEUE01接收消息：" + msg);
//    }
//
//    @RabbitListener(queues = "queue_direct02")
//    public void receive04(Object msg){
//        log.info("QUEUE02接收消息：" + msg);
//    }
//    /**
//     * topic模式，*和#通配符用于模式匹配 rouringkey
//     */
//
//    /**
//     * header模式，用的少，配置复杂，基本放弃使用！
//     */

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 下单操作
     */
    @RabbitListener(queues = "seckillQueue")
    public void receive(String msg){
        log.info("接收的消息：" + msg);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(msg, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        User user = seckillMessage.getUser();
        //判断库存
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if(goodsVo.getStockCount() < 1){
            return;
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder != null){
            return;
        }
        //下单操作
        orderService.secKill(user, goodsVo);
    }
}
























