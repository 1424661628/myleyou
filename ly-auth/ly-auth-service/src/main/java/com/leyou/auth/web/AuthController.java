package com.leyou.auth.web;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.properties.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lvmen on 2019/9/17
 */

@RestController // 实现了对用户的授权和鉴权
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties props;

    /**
     * 登录授权
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String token = authService.login(username, password);
        if (StringUtils.isBlank(token)) {
            throw new LyException(ExceptionEnum.USERNAME_OR_PASSWORD_ERROR);
        }

        // 将token写入cookie中
        CookieUtils.newBuilder(response).httpOnly() // 防止js攻击
                .maxAge(props.getCookieMaxAge())
                .request(request).build(props.getCookieName(), token);
        return ResponseEntity.ok().build();
    }

    /**
     * 校验用户登录状态
     * @return 用户登录返回UserInfo,否则抛出服务器错误
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue("LY_TOKEN") String token,
            HttpServletRequest request,HttpServletResponse response
    ) {
        try {
            // 解析token 用JwtUtils解析token
            UserInfo userInfo = JwtUtils.getUserInfo(props.getPublicKey(), token);

            // 刷新token,重新生成token
            String newToken = JwtUtils.generateToken(userInfo, props.getPrivateKey(), props.getExpire());
            // 将token写入cookie中
            CookieUtils.newBuilder(response).httpOnly()
                    .maxAge(props.getCookieMaxAge())
                    .request(request).build(props.getCookieName(), token);

            // 已登录,返回用户信息
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            // token已过期, 或者token被篡改
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }

}
