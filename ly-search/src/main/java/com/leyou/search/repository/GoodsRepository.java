package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Created by lvmen on 2019/9/12
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
