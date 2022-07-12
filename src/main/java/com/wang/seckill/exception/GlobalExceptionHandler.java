package com.wang.seckill.exception;

import com.wang.seckill.vo.RespBean;
import com.wang.seckill.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



/**
 * 全局异常处理类
 */
//如果验证Logvo的对象时 抛出异常对象e，就会扫描到@RestController + @ExceptionHandler组合注解，处理异常！
    //此注解通过  对异常的拦截实现的  统一异常返回处理
@RestControllerAdvice
public class GlobalExceptionHandler {


    //RestControllerAdvice 注解表示拦截那一层

    //ExceptionHandler 拦截什么异常类 后面的是自定义异常类

    //给这个方法加上@ExceptionHandler注解，这个方法就会处理类中其他方法（被@RequestMapping注解）抛出的异常。
    @ExceptionHandler(Exception.class)
    public RespBean ExceptionHandler(Exception e){
        if(e instanceof GlobalException){ //instanceof： e是GlobalException类的实例，返回true
            GlobalException ex = (GlobalException) e;
            return RespBean.error(ex.getRespBeanEnum());//ex.getRespBeanEnum()即getter方法获取属性
        }else if(e instanceof BindException){
            BindException ex = (BindException) e;
            RespBean respBean = RespBean.error(RespBeanEnum.BIND_ERROR);
            respBean.setMessage("参数校验异常："+ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        return RespBean.error(RespBeanEnum.ERROR);
    }
}
