package com.leyou.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lvmen on 2019/9/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private Long skuId;  //商品skuId

    private Integer num;  //购买数量
}

