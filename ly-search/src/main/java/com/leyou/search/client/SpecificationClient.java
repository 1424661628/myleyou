package com.leyou.search.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by lvmen on 2019/9/12
 */
@FeignClient("item-service")
public interface SpecificationClient extends SpecificationApi {
}
