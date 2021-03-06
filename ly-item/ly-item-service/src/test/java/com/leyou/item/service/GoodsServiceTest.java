package com.leyou.item.service;

import com.leyou.item.dto.CartDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by lvmen on 2019/9/18
 */
//@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsServiceTest {

    @Autowired
    private GoodsService goodsService;

    @Test
    public void decreaseStock() {
        List<CartDTO> list = Arrays.asList(
                new CartDTO(2600242L, 2),
                new CartDTO(2600242L, 2)
        );
        goodsService.decreaseStock(list);
    }
}