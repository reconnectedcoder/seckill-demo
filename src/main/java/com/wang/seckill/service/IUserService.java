package com.wang.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.seckill.pojo.User;
import com.wang.seckill.vo.LoginVo;
import com.wang.seckill.vo.RespBean;
import org.apache.ibatis.annotations.Mapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhoubin
 * @since 2021-12-24
 */

public interface IUserService extends IService<User> {
    /**
     * 登录
     * @param loginVo
     * @param request
     * @param response
     * @return
     */
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据cookie获取用户
     */
    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);

    /**
     * 更新密码
     */
    RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response);
}
