package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import com.leyou.user.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by lvmen on 2019/9/17
 */
@FeignClient(value = "user-service")
public interface UserClient extends UserApi {

}
