package com.wang.seckill.controller;


import com.wang.seckill.pojo.User;
import com.wang.seckill.rabbitmq.MQSender;
import com.wang.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhoubin
 * @since 2021-12-24
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MQSender mqSender;


    /**
     * 用户信息（测试）
     */
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success(user);
    }


//    /**
//     * 测试发送MQ消息
//     */
//    @RequestMapping("/mq")
//    @ResponseBody
//    public void mq(){
//        mqSender.send("hello!");
//    }
//
//    /**
//     * fanout广播模式，发送MQ消息
//     */
//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public void mq01(){
//        mqSender.send("hello!");
//    }
//
//    /**
//     * direct 直连模式，发送MQ消息
//     */
//    @RequestMapping("/mq/direct")
//    @ResponseBody
//    public void mq02(){
//        mqSender.send("hello!");
//    }
}
