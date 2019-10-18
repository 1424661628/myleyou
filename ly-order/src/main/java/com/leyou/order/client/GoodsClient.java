package com.leyou.order.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by lvmen on 2019/9/18
 */
@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {
}

