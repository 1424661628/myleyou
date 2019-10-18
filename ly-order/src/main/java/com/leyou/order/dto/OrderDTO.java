package com.leyou.order.dto;

import com.leyou.item.dto.CartDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by lvmen on 2019/9/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO { // 订单传输对象

    private Long addressId; // 收货人地址

    private Integer paymentType; // 付款类型

    private List<CartDTO> carts; // 订单详情
}
