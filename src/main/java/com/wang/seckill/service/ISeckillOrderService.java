package com.wang.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.seckill.pojo.SeckillOrder;
import com.wang.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhoubin
 * @since 2022-02-12
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {
    /*
    获取秒杀结果
     */
    Long getResult(User user, Long goodsId);
}
