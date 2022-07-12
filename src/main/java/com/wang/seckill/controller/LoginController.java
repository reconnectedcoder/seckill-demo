package com.wang.seckill.controller;

import com.wang.seckill.service.IUserService;
import com.wang.seckill.vo.LoginVo;
import com.wang.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


/**
 * 知道了每个请求都是由一个线程来处理，我们也就可以明白一个服务器同时能够处理的请求数与它的线程数有很大的关系。
 * 线程的创建是比较消耗资源的，所以容器一般维持一个线程池。
 * 像Tomcat的线程池 maxThreads 是200， minSpareThreads 是25。
 * 实际中单个Tomcat服务器的最大并发数只有几百，部分原因就是只能同时处理这么多线程上的任务。
 * 当然，并发的限制肯定不止在这里，还有很多需要考虑的地方。
 */

//RestController == @ResponseBody + @Controller, 相当于给类下面所有的方法加上@ResponseBody，返回对象，而不是页面跳转@Controller
@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {
    @Autowired
    private IUserService userService;

    /**
     * 跳转登录页面
     */
    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }
    /**
     * 登录功能
     */
    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(@Valid @RequestBody LoginVo loginVo, HttpServletRequest request, HttpServletResponse response){
        log.error("{}",loginVo);
        return userService.doLogin(loginVo,request,response);
    }
}
