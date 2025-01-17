package com.pius.im.tcp.redis;

import com.pius.im.codec.config.BootstrapConfig;
import com.pius.im.tcp.receiver.UserLoginMessageListener;
import lombok.Getter;
import org.redisson.api.RedissonClient;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
public class RedisManager {

    @Getter
    private static RedissonClient redissonClient;

    public static void init(BootstrapConfig config) {
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getIm().getRedis());
        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(config.getIm().getLoginModel());
        userLoginMessageListener.listenerUserLogin();
    }

}
