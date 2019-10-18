package com.leyou.order.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.auth.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.interceptors.LoginInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lvmen on 2019/9/18
 */
@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private PayHelper payHelper;
    @Autowired
    private PayLogService payLogService;
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 生成订单
     * @param orderDto
     * @return 返回订单id
     */
    @Transactional
    public Long createOrder(OrderDTO orderDto) {  // 三张表数据的插入

        // 1.1 填充order，订单中的用户信息数据从Token中获取，填充到order中
        Order order = new Order();

        // 1.2 生成订单ID，采用雪花算法生成订单ID
        long orderId = idWorker.nextId();
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setPaymentType(orderDto.getPaymentType());
        order.setPostFee(0L);  //// TODO 调用物流信息，根据地址计算邮费

        // 1.3 获取用户信息
        UserInfo user = LoginInterceptor.getLoginUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getName());
        order.setBuyerRate(false);  //卖家为留言
        // 1.4 收货人地址信息，应该从数据库中物流信息中获取，这里使用的是假的数据
        AddressDTO addressDTO = AddressClient.findById(orderDto.getAddressId());
        if (addressDTO == null) {
            throw new LyException(ExceptionEnum.RECEIVER_ADDRESS_NOT_FOUND);
        }
        order.setReceiver(addressDTO.getName());
        order.setReceiverAddress(addressDTO.getAddress());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverMobile(addressDTO.getPhone());
        order.setReceiverZip(addressDTO.getZipCode());
        order.setReceiverState(addressDTO.getState());

        // 1.4 金额
        // 付款金额相关，把orderDto转化成map，key为skuId,值为购物车中该sku的购买数量
        Map<Long, Integer> skuNumMap = orderDto.getCarts().stream()
                .collect(Collectors.toMap(c -> c.getSkuId(), c -> c.getNum()));
        // 查询商品信息，根据skuIds批量查询sku详情
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(skuNumMap.keySet()));

        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        Double totalPay = 0.0;
        // 填充orderDetail
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        // 遍历skus，填充orderDetail
        for (Sku sku : skus) {
            Integer num = skuNumMap.get(sku.getId());
            totalPay += num * sku.getPrice();

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setNum(num);
            orderDetail.setPrice(sku.getPrice().longValue());
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));

            orderDetails.add(orderDetail);
        }
        order.setActualPay((totalPay.longValue() + order.getPostFee()));  //todo 还要减去优惠金额
        order.setTotalPay(totalPay.longValue());

        // 1.5 新增订单
        orderMapper.insertSelective(order);
        // 2 新增订单详情
        orderDetailMapper.insertList(orderDetails);
        // 3 新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        orderStatus.setCreateTime(new Date());
        orderStatusMapper.insertSelective(orderStatus);
        // 4 减库存 todo 同步调用还是异步调用.
        goodsClient.decreaseStock(orderDto.getCarts());

        //todo 删除购物车中已经下单的商品数据, 采用异步mq的方式通知购物车系统删除已购买的商品，传送商品ID和用户ID
        HashMap<String, Object> map = new HashMap<>();
        try {
            map.put("skuIds", skuNumMap.keySet());
            map.put("userId", user.getId());
            amqpTemplate.convertAndSend("ly.cart.exchange", "cart.delete", JsonUtils.toString(map));
        } catch (Exception e) {
            log.error("删除购物车消息发送异常，商品ID：{}", skuNumMap.keySet(), e);
        }
        log.info("生成订单，订单编号：{}，用户id：{}", orderId, user.getId());
        return orderId;
    }

    /**
     * 查询订单
     * @param id 订单ID
     * @return
     */
    public Order queryOrderById(Long id) {
        // 查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {            // 不存在
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUNT);
        }
        // 查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = orderDetailMapper.select(detail);
        if (CollectionUtils.isEmpty(details)) {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(details);
        // 查询订单状态
        OrderStatus status = orderStatusMapper.selectByPrimaryKey(id);
        if (status == null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(status);
        return order;
    }


    /**
     * 微信下单
     * @param orderId 订单id
     * @return 成功返回支付的url, 失败抛出异常
     */
    public String createPayUrl(Long orderId) {
        // 查询订单
        Order order = queryOrderById(orderId);
        // 判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.INIT.value()){
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        // 支付金额
        Long actualPay = order.getActualPay();
        // 商品描述
        OrderDetail orderDetail = order.getOrderDetails().get(0);
        String desc = orderDetail.getTitle();
        return payHelper.createPayUrl(orderId, actualPay, desc);
    }


    /**
     * 校验数据
     * @param msg
     */
    @Transactional
    public void handleNotify(Map<String, String> msg) {
        payHelper.handleNotify(msg);
    }

    /**
     *
     * @param page
     * @param rows
     * @return
     */
    public PageResult<Order> queryOrderByPage(Integer page, Integer rows) {

        //开启分页
        PageHelper.startPage(page, rows);

        Example example = new Example(Order.class);

        //查询订单
        List<Order> orders = orderMapper.selectByExample(example);


        //查询订单详情
        for (Order order : orders) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getOrderId());
            List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);

            order.setOrderDetails(orderDetailList);

            //查询订单状态
            OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(order.getOrderId());
            order.setOrderStatus(orderStatus);
        }

        PageInfo<Order> pageInfo = new PageInfo<>(orders);

        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), pageInfo.getList());
    }

}
