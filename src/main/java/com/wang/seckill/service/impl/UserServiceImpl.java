package com.wang.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.seckill.exception.GlobalException;
import com.wang.seckill.mapper.UserMapper;
import com.wang.seckill.pojo.User;
import com.wang.seckill.service.IUserService;
import com.wang.seckill.utils.CookieUtil;
import com.wang.seckill.utils.MD5Util;
import com.wang.seckill.utils.UUIDUtil;
import com.wang.seckill.vo.LoginVo;
import com.wang.seckill.vo.RespBean;
import com.wang.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhoubin
 * @since 2021-12-24
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录
     * @param loginVo
     * @param request
     * @param response
     * @return
     */
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        //从前端获取手机号和密码
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        //参数校验
//        if(StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//        if(!ValidatorUtil.isMobile(mobile)){
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }

        //根据手机号获取用户, 从数据库获取，返回user对象
        User user = userMapper.selectById(mobile);
        log.info(userMapper.toString());
        //全局异常处理抛给前端处理。
        if(null == user){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        //判断密码是否正确
        if(!MD5Util.formPassToDBPass(password,user.getSalt()).equals(user.getPassword())){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
//          return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        //生成cookie
        String ticket = UUIDUtil.uuid();
        //将用户信息存入redis中
        redisTemplate.opsForValue().set("user:" + ticket,user);
//        request.getSession().setAttribute(ticket,user);
        CookieUtil.setCookie(request,response,"userTicket",ticket);
        return RespBean.success(ticket);
    }




    /**
     * 根据cookie获取用户
     * @param userTicket
     * @return
     */
    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if(StringUtils.isEmpty(userTicket)){
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        if(user!=null) {
            CookieUtil.setCookie(request,response,"userTicket",userTicket);
        }

        return user;
    }

    /**
     * 更新密码
     * @param userTicket
     * @param password
     * @return
     */
    @Override
    public RespBean updatePassword(String userTicket, String password,
                                   HttpServletRequest request, HttpServletResponse response) {
        User user = getUserByCookie(userTicket,request,response);
        if(user == null){
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.inputPassToDBPass(password,user.getSalt()));
        int result = userMapper.updateById(user);
        if(1 == result){
            //删除redis
            redisTemplate.delete("user:" + userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.UPDATE_FAILED);
    }
}
