package com.leyou.page.mq;

import com.leyou.page.service.PageService;
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
    private PageService pageService;

    /**
     * 监听商品的新增和更新, 新增静态页面
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.insert.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
    ))
    public void listenInsertOrUpdate(Long spuId){
        if (spuId == null){
            return;
        }
        // 处理消息, 对索引库进行新增或修改
        pageService.createHtml(spuId);
    }

    /**
     * 监听商品的删除, 删除静态页面
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.insert.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void listenDelete(Long spuId){
        if (spuId == null){
            return;
        }
        // 处理消息, 对索引库进行新增或修改
        pageService.deleteHtml(spuId);
    }
}
