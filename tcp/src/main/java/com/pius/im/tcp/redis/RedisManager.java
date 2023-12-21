package com.pius.im.tcp.redis;

import com.pius.im.codec.config.BootstrapConfig;
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
    }

}
