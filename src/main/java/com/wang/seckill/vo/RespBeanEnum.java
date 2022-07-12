package com.wang.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 公共返回对象枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {
    //通用
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务器端异常"),

    //登录模块
    LOGIN_ERROR(500210,"用户名或密码错误"),
    MOBILE_ERROR(500211,"手机号不正确"),
    BIND_ERROR(500212,"参数校验异常"),
    MOBILE_NOT_EXIST(500213,"手机号不存在"),
    UPDATE_FAILED(500214,"更新密码失败"),
    SESSION_ERROR(500215,"用户不存在"),

    //订单模块5003xx
    ORDER_NOT_EXIST(500300,"订单不存在"),

    //秒杀模块5005xx
    EMPTY_STOCK(500500,"库存不足"),
    REPEATE_ERROR(500501,"该商品没人限购一件"),
    REQUEST_ILLEGAL(500502,"请求非法，请重试"),
    ERROR_CAPTCHA(500503,"验证码错误，请重试"),
    ACCESS_LIMIT_REAHCED(500504,"访问过于频繁，请稍后再试")
    ;

    
    private final Integer code;
    private final String message;
}
