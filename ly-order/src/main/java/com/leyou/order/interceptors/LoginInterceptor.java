package com.leyou.order.interceptors;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by lvmen on 2019/9/17
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private JwtProperties props;

    // 定义一个线程域,存放登录的对象
    private static ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public LoginInterceptor(JwtProperties prop) {
        this.props = prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 先获取token
        String token = CookieUtils.getCookieValue(request, props.getPubKeyPath());
        if (StringUtils.isBlank(token)) {
            //用户未登录,返回401，拦截
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        // 用户已登录,获取用户信息
        try {
            UserInfo userInfo = JwtUtils.getUserInfo(props.getPublicKey(), token);
            // 放入线程域
            tl.set(userInfo);
            return true;
        } catch (Exception e){
            // 抛出异常, 未登录
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 最后用完数据,一定要清空
        tl.remove();
    }

    public static UserInfo getLoginUser(){
        return tl.get();
    }
}
