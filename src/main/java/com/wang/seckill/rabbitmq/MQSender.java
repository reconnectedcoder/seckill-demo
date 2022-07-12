package com.wang.seckill.rabbitmq;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

//    public void send(Object msg){
//        log.info("发送消息：" + msg);
//        rabbitTemplate.convertAndSend("directExchange", "queue.red", msg);
//    }

    /**
     * 发送秒杀信息
     * @param msg
     */
    public void sendSeckillMessage(String msg){
        log.info("发送消息：" + msg);
        rabbitTemplate.convertAndSend("seckillExchange", "seckill.message", msg);
    }

}
