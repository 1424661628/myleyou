package com.leyou.search.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.api.GoodsApi;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by lvmen on 2019/9/12
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {

}
