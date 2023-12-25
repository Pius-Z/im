package com.pius.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.ImConnectStatusEnum;
import com.pius.im.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: Pius
 * @Date: 2023/12/25
 */
@Component
public class UserSessionUtils {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 获取用户所有的session
     */
    public List<UserSession> getUserSession(Integer appId, String userId) {
        List<UserSession> list = new ArrayList<>();

        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants + userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(userSessionKey);

        for (Object o : entries.values()) {
            String str = (String) o;
            UserSession session = JSONObject.parseObject(str, UserSession.class);
            if (session.getConnectState().equals(ImConnectStatusEnum.ONLINE_STATUS.getCode())) {
                list.add(session);
            }
        }

        return list;
    }

    /**
     * 获取指定端的session
     */
    public UserSession getUserSession(Integer appId, String userId, Integer clientType, String imei) {

        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants + userId;
        String hashKey = clientType + ":" + imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);

        if (o != null) {
            return JSONObject.parseObject(o.toString(), UserSession.class);
        }

        return null;
    }

}
