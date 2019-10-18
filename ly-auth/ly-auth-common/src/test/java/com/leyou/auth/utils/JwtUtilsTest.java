package com.leyou.auth.utils;

import com.leyou.auth.entity.UserInfo;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by lvmen on 2019/9/17
 */
public class JwtUtilsTest {
    private static final String publicKeyPath = "D:\\uploads\\rsa.pub";
    private static final String privateKeyPath = "D:\\uploads\\rsa.pri";

    private PrivateKey privateKey;
    private PublicKey publicKey;


    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(publicKeyPath, privateKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        publicKey = RsaUtils.getPublicKey(publicKeyPath);
    }

    /**
     * 测试生成token
     */
    @Test
    public void testGenerateToken() {
        //生成Token
        String s = JwtUtils.generateToken(new UserInfo(20L, "Jack"), privateKey, 5);
        System.out.println("s = " + s);
    }


    /**
     * 测试解析token
     */
    @Test
    public void parseToken() {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiSmFjayIsImV4cCI6MTUzODM2OTM3N30.Vi7UJrwMu0BOHMQoSSLefzGU1ir5LG-drcvAHPjMMMBzQz1oASjoDsiuw3h0bqVUUWXjdNcpybCWVuZ8UvOXOr-Jecqjz3NF_ZDfgessRGsijIIbju0qak6Zfm09jsjnHFTZ2munFJdM0I0RsiafQtkJSiLji7QXlvjCquKJUkg";
        UserInfo userInfo = JwtUtils.getUserInfo(publicKey, token);
        System.out.println("id:" + userInfo.getId());
        System.out.println("name:" + userInfo.getName());
    }

    @Test
    public void parseToken1() {
    }

    @Test
    public void getUserInfo() {
    }

    @Test
    public void getUserInfo1() {
    }
}
