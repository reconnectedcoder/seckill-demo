package com.wang.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.seckill.exception.GlobalException;
import com.wang.seckill.mapper.OrderMapper;
import com.wang.seckill.pojo.Order;
import com.wang.seckill.pojo.SeckillGoods;
import com.wang.seckill.pojo.SeckillOrder;
import com.wang.seckill.pojo.User;
import com.wang.seckill.service.IGoodsService;
import com.wang.seckill.service.IOrderService;
import com.wang.seckill.service.ISeckillGoodsService;
import com.wang.seckill.service.ISeckillOrderService;
import com.wang.seckill.utils.MD5Util;
import com.wang.seckill.utils.UUIDUtil;
import com.wang.seckill.vo.GoodsVo;
import com.wang.seckill.vo.OrderDetailVo;
import com.wang.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhoubin
 * @since 2022-02-12
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Autowired
    private ISeckillGoodsService seckillGoodsService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 秒杀
     * @param user
     * @param goods
     * @return
     */

    /**
     * 方式一：spring的xml配置方式
     * <!-- 1.配置事务的管理器 -->
     * 	<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
     * 	    <!-- 指定要对哪个数据库进行事务操作 -->
     * 	    <property name="dataSource" ref="dataSource"></property>
     * 	</bean>
     *
     * 	<!-- 2.开启事务的注解 -->
     * 	<tx:annotation-driven transaction-manager="transactionManager">	</tx:annotation-driven>
     *
     * 	方式二：注解方式，springboot 的 @Transactionnal 注解实现的声明式事务，
     * 	原理：基于AOP的动态代理，如果被@Transational注解
     * 	Spring有一个针对@Transactional的增强器（拦截器）Interceptor，
     * 	在bean实例初始化的最后一步会调用带该拦截器的拦截器链增强@Transactional注解的方法，并且生成代理类
     * 	Spring的Interceptior拦截器实现事务的方式和我们在DAO层手动实现事务差不多，
     * 	都是先开启事务，再执行，如果异常则回滚，否则提交事务，只不过这里把执行的逻辑换成了递归执行拦截链了
     */

    //用spring的声明式事务保证数据库生成订单，扣减库存的原子性。
    @Transactional
    @Override
    public Order secKill(User user, GoodsVo goods) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //秒杀商品表减库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id",
                goods.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);//

        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = stock_count-1")
                .eq("goods_id", goods.getId()).gt("stock_count",0));

        //判断是否还有库存
        if(!result){
            valueOperations.set("isStockEmpty:" + goods.getId(),"0");
            return null;
        }
        
        //生成商品订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);

        //生成秒杀商品订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);
        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goods.getId(), seckillOrder);
        return order;
    }

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    @Override
    public OrderDetailVo detail(Long orderId) {
        if(orderId == null){
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo detail = new OrderDetailVo();
        detail.setOrder(order);
        detail.setGoodsVo(goodsVo);
        return detail;
    }

    /**
     * 获取秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" + goodsId, str, 60, TimeUnit.SECONDS);
        return str;
    }

    /**
     * 校验秒杀地址
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if(user == null || goodsId < 0 || StringUtils.isEmpty(path)){
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }

    /**
     * 校验验证码
     * @param user
     * @param goodsId
     * @param captcha
     * @return
     */
    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if(StringUtils.isEmpty(captcha) || user == null || goodsId < 0){
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }
}
