package com.leyou.page.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by lvmen on 2019/9/12
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {

}
