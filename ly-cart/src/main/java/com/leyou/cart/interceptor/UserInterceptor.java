package com.leyou.cart.interceptor;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lvmen on 2019/9/17
 */
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties props;


    private static ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public UserInterceptor(JwtProperties prop) {
        this.props = prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 先获取cookie
        String token = CookieUtils.getCookieValue(request, props.getCookieName());

        try {
            // 解析token
            UserInfo user = JwtUtils.getUserInfo(props.getPublicKey(), token);
            // 传递user @线程共享
            tl.set(user);
            // 放行
            return true;
        } catch (Exception e) {
            log.error("[购物车服务] 解析用户身份失败", e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 最后用完数据,一定要清空
        tl.remove();
    }

    public static UserInfo getUser(){
        return tl.get();
    }
}
