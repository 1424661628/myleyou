package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by lvmen on 2019/9/3
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {

    // 商品分类
    CATEGORY_NOT_FOUND(404, "商品分类不存在!"),

    // 品牌
    BRAND_NOT_FOUND(404, "品牌不存在!"),
    BRAND_SAVE_ERROR(500, "品牌新增失败!"),
    BRAND_DELETE_ERROR(500, "品牌删除失败!"),

    // 文件上传
    UPLOAD_FILE_ERROR(500, "文件上传失败!"),
    INVALID_FILE_TYPE(400, "无效的文件类型"),

    // 规格参数
    GROUP_NOT_FOUND(404, "规格组不存在"),
    GROUP_CREATE_ERROR(500, "规格组新增失败"),

    PARAM_NOT_FOUND(404, "商品规格参数不存在"),
    PARAM_SAVE_ERROR(500, "新增商品规格参数失败"),
    PARAM_UPDATE_ERROR(500, "修改商品规格参数失败"),
    PARAM_DELETE_ERROR(500, "删除商品规格参数失败"),

    // 商品
    GOODS_NOT_FOUND(404, "商品不存在"),
    GOODS_SAVE_ERROR(500, "商品新增失败"),
    GOODS_UPDATE_ERROR(500, "商品修改失败"),
    DELETE_GOODS_ERROR(500, "删除商品失败"),
    GOODS_SKU_NOT_FOUND(404, "商品SKU不存在"),
    // 商品详情
    GOODS_DETAIL_NOT_FOUND(404 ,"商品详情不存在"),
    GOODS_DETAIL_SAVE_ERROR(500,"商品详情新增失败"),

    GOODS_STOCK_NOT_FOUND(404, "商品库存不存在"),
    STOCK_SAVE_ERROR(500, "商品库存新增失败"),
    STOCK_NOT_ENOUGH(500, "商品库存不足"),

    INVALID_PARAM(500, "非法参数"),
    INVALID_USER_DATA_TYPE(400,"用户数据类型无效"),
    VERIFY_CODE_NOT_MATCHING(400,"验证码错误"),

    USER_NOT_EXIST(404, "用户不存在"),
    PASSWORD_NOT_MATCHING(400,"密码错误"),
    USERNAME_OR_PASSWORD_ERROR(400,"用户名或密码不正确"),

    CREATE_TOKEN_ERROR(500, "用户凭证生成失败"),
    UNAUTHORIZED(403,"未授权"),

    CART_NOT_FOUND(404, "购物车为空"),


    RECEIVER_ADDRESS_NOT_FOUND(404,"收货地址不存在"),

    ORDER_NOT_FOUNT(404, "订单不存在"),
    ORDER_DETAIL_NOT_FOUND(404, "订单详情不存在"),
    ORDER_STATUS_NOT_FOUND(404, "订单状态不存在"),
    ORDER_STATUS_ERROR(500, "订单状态有误"),

    WX_PAY_ORDER_FAIL(500, "微信支付下单失败"),
    WX_PAY_SIGN_INVALID(500,"微信支付校验签名失败"),
    WX_PAY_NOTIFY_PARAM_ERROR(400, "微信支付参数异常")

    ;

    private int code;
    private String msg;

}
