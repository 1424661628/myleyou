package com.leyou.cart.web;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by lvmen on 2019/9/17
 */
@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加商品到购物车
     * @param cart 购物车中商品
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询购物车列表
     * @return 购物车列表
     */
    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList(){
        return ResponseEntity.ok(cartService.queryCartList());
    }

    /**
     * 修改购物车的商品数量
     * @param skuId 商品ID
     * @param num 商品数量
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateCartNum(@RequestParam("id") Long skuId, @RequestParam("num") Integer num){
        cartService.updateNum(skuId,num);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 从购物车中删除指定商品
     * @param skuId 商品ID
     * @return
     */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable("id")Long skuId){
        cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
