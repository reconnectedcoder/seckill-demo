package com.wang.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wang.seckill.config.AccessLimit;
import com.wang.seckill.exception.GlobalException;
import com.wang.seckill.pojo.Order;
import com.wang.seckill.pojo.SeckillMessage;
import com.wang.seckill.pojo.SeckillOrder;
import com.wang.seckill.pojo.User;
import com.wang.seckill.rabbitmq.MQSender;
import com.wang.seckill.service.IGoodsService;
import com.wang.seckill.service.IOrderService;
import com.wang.seckill.service.ISeckillOrderService;
import com.wang.seckill.utils.JsonUtil;
import com.wang.seckill.vo.GoodsVo;
import com.wang.seckill.vo.RespBean;
import com.wang.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/seckill")
@Slf4j
public class SecKillController implements InitializingBean {    //初始化bean 的接口
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisScript<Long> script;

    private Map<Long, Boolean> emptyStockMap = new HashMap<>();

    /**
     * 秒杀
     */
    @RequestMapping("/doSeckill2")
    public String doSecKill2(Model model, User user, Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if (goods.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        //判断是否重复抢购
        SeckillOrder seckillorder =
                seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId())
                        .eq("goods_id", goodsId));//eq()查询方法
        if (seckillorder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }
        Order order = orderService.secKill(user, goods);
        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";

    }


    /**
     * 获取秒杀结果
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 秒杀
     */
    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(@PathVariable String path, User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        boolean check = orderService.checkPath(user, goodsId, path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //判断是否重复抢购 or 多次请求
        SeckillOrder seckillorder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillorder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //内存标记，减少redis访问!!
        if (emptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存!!
//        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        //采用Redis + Lua 脚本，将 （加锁，获取锁，比较锁，释放锁）原子执行！
        Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId),
                Collections.EMPTY_LIST);
        if (stock < 0) {
            emptyStockMap.put(goodsId, true);
            //为了最后看起来好看，库存最后为0，而不是-1；
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));

        return RespBean.success(0);
    }


    /**
     * 获取秒杀地址
     */
    @AccessLimit(second=5,maxCount=5,needLogin=true)//5s内超过5次请求，需要登录
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request) {
//        if (user == null) {
//            return RespBean.error(RespBeanEnum.SESSION_ERROR);
//        }

//        //限流计数器算法！
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        //限制访问次数，5秒内访问5次
//        String uri = request.getRequestURI();
//        captcha = "0";
//        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
//        if(count == null){
//            valueOperations.set(uri + ":" + user.getId(),1,5, TimeUnit.SECONDS);
//        }else if(count < 5){
//            valueOperations.increment(uri + ":" + user.getId());
//        }else{
//            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REAHCED);
//        }


        //校验验证码
        boolean check = orderService.checkCaptcha(user,goodsId,captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    /**
     * 获取验证码
     * @param user
     * @param goodsId
     * @param response
     */
    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    @ResponseBody
    public void virifyCode(User user, Long goodsId, HttpServletResponse response) {
        if (user == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置请求头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Param", "No-cache");
        response.setHeader("Cache-control", "no-cache");
        response.setDateHeader("Expires", 0);
        //生成验证码，将结果放入redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);

        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }

        /*

        //访问数据库，查库存
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if(goods.getStockCount() < 1){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //判断是否重复抢购
//        SeckillOrder seckillorder =
//                seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId())
//                        .eq("goods_id", goodsId));//eq()查询方法

        SeckillOrder seckillorder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);

        if(seckillorder != null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //访问数据库，生成订单
        Order order = orderService.secKill(user,goods);
        return RespBean.success(order);

         */


    /**
     * 系统初始化，把商品库存数量加载到Redis
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) return;
        //数据库找到库存放到redis中
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            emptyStockMap.put(goodsVo.getId(), false);
        });
    }
}
