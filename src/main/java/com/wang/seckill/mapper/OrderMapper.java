package com.wang.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.seckill.pojo.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhoubin
 * @since 2022-02-12
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}
