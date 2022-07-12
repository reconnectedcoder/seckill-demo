package com.wang.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.seckill.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhoubin
 * @since 2021-12-24
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
