package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import static com.github.wxpay.sdk.WXPayConstants.*; // 静态导入 JDK5的新特性

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayConfig;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayStateEnum;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.mapper.PayLogMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.pojo.PayLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvmen on 2019/9/18
 */
@Slf4j
@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;

    @Autowired
    private PayConfig config;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private PayLogMapper payLogMapper;
    @Autowired
    private OrderStatusMapper statusMapper;


    /**
     * 创建支付url
     * @param orderId 订单id
     * @param totalPay 支付金额
     * @param desc 商品描述
     * @return 成功返回微信支付url, 失败抛出异常
     */
    public String createPayUrl(Long orderId, Long totalPay, String desc) {
        try {
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body", desc);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            // 金额, 单位是分
            data.put("total_fee", totalPay.toString());
            // 调用微信支付的终端IP
            data.put("spbill_create_ip", "127.0.0.1");
            // 回调地址
            data.put("notify_url",config.getNotifyUrl());
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");

            // 利用wxPay工具, 完成下单
            Map<String, String> result = wxPay.unifiedOrder(data);

            // 判断通信标识
            String return_code = result.get("return_code");
            if (FAIL.equals(return_code)){
                // 通信失败
                log.error("[微信下单] 微信下单通信失败,失败原因:{}", result.get("return_msg"));
                throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
            }
            // 判断业务标识
            String result_code = result.get("result_code");
            if (FAIL.equals(result_code)){
                // 通信失败
                log.error("[微信下单] 微信下单通信失败,错误码:{}, 错误原因:{}",
                        result.get("error_code"), result.get("error_code_desc"));
                throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
            }

            // 下单成功, 获取支付链接
            String url = result.get("code_url");
            return url;

        }catch (Exception e){
            log.error("[微信下单] 创建预交易订单异常失败", e);
            return null;
        }
    }


    /**
     * 检验签名
     * @param result
     */
    private void isSignatureValid(Map<String, String> result) {
        try {
            boolean boo1 = WXPayUtil.isSignatureValid(result, config.getKey(), SignType.HMACSHA256);
            boolean boo2 = WXPayUtil.isSignatureValid(result, config.getKey(), SignType.MD5);

            if (!boo1 && !boo2) {
                throw new LyException(ExceptionEnum.WX_PAY_SIGN_INVALID);
            }
        } catch (Exception e) {
            log.error("【微信支付】检验签名失败，数据：{}", result);
            throw new LyException(ExceptionEnum.WX_PAY_SIGN_INVALID);
        }
    }

    /**
     * 处理回调结果
     * @param msg
     */
    public void handleNotify(Map<String, String> msg) {
        //检验签名
        isSignatureValid(msg);

        //检验金额
        //解析数据
        String totalFee = msg.get("total_fee");  //订单金额
        String outTradeNo = msg.get("out_trade_no");  //订单编号
        String transactionId = msg.get("transaction_id");  //商户订单号
        String bankType = msg.get("bank_type");  //银行类型
        if (StringUtils.isBlank(totalFee) || StringUtils.isBlank(outTradeNo)
                || StringUtils.isBlank(transactionId) || StringUtils.isBlank(bankType)) {
            log.error("【微信支付回调】支付回调返回数据不正确");
            throw new LyException(ExceptionEnum.WX_PAY_NOTIFY_PARAM_ERROR);
        }

        //查询订单
        Order order = orderMapper.selectByPrimaryKey(Long.valueOf(outTradeNo));

        //todo 这里验证回调数据时，支付金额使用1分进行验证，后续使用实际支付金额验证
        if (/*order.getActualPay()*/1 != Long.valueOf(totalFee)) {
            log.error("【微信支付回调】支付回调返回数据不正确");
            throw new LyException(ExceptionEnum.WX_PAY_NOTIFY_PARAM_ERROR);

        }

        //判断支付状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(Long.valueOf(outTradeNo));

        if (orderStatus.getStatus() != OrderStatusEnum.INIT.value()) {
            //支付成功
            return;
        }

        //修改支付日志状态
        PayLog payLog = payLogMapper.selectByPrimaryKey(order.getOrderId());
        //未支付的订单才需要更改
        if (payLog.getStatus() == PayStateEnum.NOT_PAY.getValue()) {
            payLog.setOrderId(order.getOrderId());
            payLog.setBankType(bankType);
            payLog.setPayTime(new Date());
            payLog.setTransactionId(transactionId);
            payLog.setStatus(PayStateEnum.SUCCESS.getValue());
            payLogMapper.updateByPrimaryKeySelective(payLog);
        }


        //修改订单状态
        OrderStatus orderStatus1 = new OrderStatus();
        orderStatus1.setStatus(OrderStatusEnum.PAY_UP.value());
        orderStatus1.setOrderId(order.getOrderId());
        orderStatus1.setPaymentTime(new Date());
        statusMapper.updateByPrimaryKeySelective(orderStatus1);
    }


    /**
     * 查询订单支付状态
     * @param orderId
     * @return
     */
    public PayStateEnum queryPayState(Long orderId) {
        Map<String, String> data = new HashMap<>();
        data.put("out_trade_no", orderId.toString());
        try {
            Map<String, String> result = wxPay.orderQuery(data);
            if (CollectionUtils.isEmpty(result) || WXPayConstants.FAIL.equals(result.get("return_code"))) {
                //未查询到结果，或连接失败
                log.error("【支付状态查询】链接微信服务失败，订单编号：{}", orderId);
                return PayStateEnum.NOT_PAY;
            }
            //查询失败
            if (WXPayConstants.FAIL.equals(result.get("result_code"))) {
                log.error("【支付状态查询】查询微信订单支付结果失败，错误码：{}, 订单编号：{}", result.get("result_code"), orderId);
                return PayStateEnum.NOT_PAY;
            }
            //检验签名
            isSignatureValid(result);

            //查询支付状态
            String state = result.get("trade_state");
            if (StringUtils.equals("SUCCESS", state)) {
                //支付成功, 修改支付状态等信息
                handleNotify(result);
                return PayStateEnum.SUCCESS;
            } else if (StringUtils.equals("USERPAYING", state) || StringUtils.equals("NOTPAY", state)) {
                //未支付成功
                return PayStateEnum.NOT_PAY;
            } else {
                //其他返回付款失败
                return PayStateEnum.FAIL;
            }
        } catch (Exception e) {
            log.error("查询订单支付状态异常", e);
            return PayStateEnum.NOT_PAY;
        }

    }
}
