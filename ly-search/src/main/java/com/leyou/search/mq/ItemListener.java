package com.leyou.search.mq;


import com.leyou.search.service.SearchService;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by lvmen on 2019/9/16
 */
@Component
public class ItemListener {

    @Autowired
    private SearchService searchService;

    /**
     * 监听商品的修改和新增, 修改索引库商品的信息
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.insert.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
    ))
    public void listenInsertOrUpdate(Long spuId){
        if (spuId == null){
            return;
        }
        // 处理消息, 对索引库进行新增或修改
        searchService.createOrUpdateIndex(spuId);
    }

    /**
     * 监听商品的删除, 删除索引库中商品的信息
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.delete.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void listenDelete(Long spuId){
        if (spuId == null){
            return;
        }
        // 处理消息, 对索引库进行新增或修改
        searchService.deleteIndex(spuId);
    }
}
