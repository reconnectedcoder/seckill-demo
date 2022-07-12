package com.wang.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfigDirect {

//    private static final String QUEUE01 = "queue_direct01";
//    private static final String QUEUE02 = "queue_direct02";
//    private static final String EXCHANGE = "directExchange";
//    private static final String ROUTINGKEY01 = "queue.red";
//    private static final String ROUTINGKEY02 = "queue.green";
//
//    /**
//     * 定义两个队列
//     * @return
//     */
//    @Bean
//    public Queue queue01(){
//        return new Queue(QUEUE01);
//    }
//
//    @Bean
//    public Queue queue02(){
//        return new Queue(QUEUE02);
//    }
//
//    /**
//     * 定义一个交换机
//     * @return
//     */
//    @Bean
//    public DirectExchange directExchange(){
//        return new DirectExchange(EXCHANGE);
//    }
//
//    /**
//     * 绑定队列，交换机，路由键；即：bingdingbuiler.bind(队列).to(交换机).with(路由键)
//     * @return
//     */
//    @Bean
//    public Binding binding01(){
//        return BindingBuilder.bind(queue01()).to(directExchange()).with(ROUTINGKEY01);
//    }
//
//    @Bean
//    public Binding binding02(){
//        return BindingBuilder.bind(queue02()).to(directExchange()).with(ROUTINGKEY02);
//    }

    private static final String QUEUE = "seckillQueue";
    private static final String EXCHANGE = "seckillExchange";

    @Bean
    public Queue queue(){
        return new Queue(QUEUE);
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(topicExchange()).with("seckill.#");
    }
}
