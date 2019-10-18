package com.leyou.auth.properties;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by lvmen on 2019/9/17
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {

    private String secret;

    private String pubKeyPath;

    private String priKeyPath;

    private Integer expire; // 到期时间

    private String cookieName;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    private int cookieMaxAge;

    // 对象一旦实例化后,就应该读取公钥和私钥
    @PostConstruct
    public void init() {
        try {
            //首先判断公钥私钥是否存在，不存在则先生成公钥私钥
            File pubKey = new File(pubKeyPath);
            File priKey = new File(priKeyPath);

            if (!pubKey.exists() || !priKey.exists()) {
                //创建公钥，私钥
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }
            //公钥私钥都存在
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥私钥失败",e);
            throw new RuntimeException();
        }
    }
}
