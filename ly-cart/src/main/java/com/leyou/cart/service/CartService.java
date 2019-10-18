package com.leyou.cart.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lvmen on 2019/9/17
 */
@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public static final String KEY_PREFIX = "cart:uid:";

    /**
     * 将商品添加到购物车
     * @param cart
     */
    public void addCart(Cart cart) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // 判断当前购物车商品,是否存在
        String key = KEY_PREFIX + user.getId();
        String hashKey = cart.getSkuId().toString();
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        if (operation.hasKey(hashKey)) {
            // 是,修改数量
            String json = operation.get(hashKey).toString();
            Cart cacheCart = JsonUtils.parse(json, Cart.class);
            cacheCart.setNum(cart.getNum());
            // 写回redis
            operation.put(hashKey, JsonUtils.toString(cacheCart));
        }else {
            // 否,新增
            operation.put(hashKey, JsonUtils.toString(cart));
        }
    }

    /**
     * 查询购物车列表
     * @return
     */
    public List<Cart> queryCartList() {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        if (!redisTemplate.hasKey(key)){
            // key 不存在,返回404
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        // 获取登录用户的所有购物车
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        List<Object> carts = operation.values();
        if (CollectionUtils.isEmpty(carts)) {
            //购物车中无数据
            return null;
        }
        return carts.stream().map(s -> JsonUtils.parse(s.toString(), Cart.class)).collect(Collectors.toList());
    }

    /**
     * 更新购物车中指定商品的数量
     * @param skuId
     * @param num
     */
    public void updateNum(Long skuId, Integer num) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        String hashKey = skuId.toString();
        // 获取操作
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);

        // 判断是否存在
        if (!operation.hasKey(hashKey)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }

        // 查询购物车
        Cart cart = JsonUtils.parse(operation.get(hashKey).toString(), Cart.class);
        cart.setNum(num);

        // 写回redis
        operation.put(hashKey, JsonUtils.toString(cart));

    }

    /**
     * 根据商品id删除购物车中商品
     * @param skuId
     */
    public void deleteCart(Long skuId) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // key
        String key = KEY_PREFIX + user.getId();
        // 删除
        redisTemplate.opsForHash().delete(key,skuId.toString());
    }
}
