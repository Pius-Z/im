package com.pius.im.service.user.service.impl;

import com.pius.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.command.UserEventCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.common.model.UserSession;
import com.pius.im.service.friendship.service.ImFriendService;
import com.pius.im.service.user.model.UserStatusChangeNotifyContent;
import com.pius.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.pius.im.service.user.service.ImUserStatusService;
import com.pius.im.service.utils.MessageProducer;
import com.pius.im.service.utils.UserSessionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @Author: Pius
 * @Date: 2024/1/4
 */
@Service
public class ImUserStatusServiceImpl implements ImUserStatusService {

    @Autowired
    ImFriendService imFriendService;

    @Autowired
    UserSessionUtils userSessionUtils;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content) {

        List<UserSession> userSession = userSessionUtils.getUserSession(content.getAppId(), content.getUserId());
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        BeanUtils.copyProperties(content, userStatusChangeNotifyPack);
        userStatusChangeNotifyPack.setClient(userSession);

        syncSender(userStatusChangeNotifyPack, content.getUserId(), content);

        dispatcher(userStatusChangeNotifyPack, content.getUserId(), content.getAppId());
    }

    private void syncSender(Object pack, String userId, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(userId, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC,
                pack, clientInfo);
    }

    private void dispatcher(Object pack, String userId, Integer appId) {
        List<String> allFriendId = imFriendService.getAllFriendId(userId, appId);
        for (String friendId : allFriendId) {
            messageProducer.sendToUser(friendId, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY, pack, appId);
        }

        String userKey = appId + ":" + Constants.RedisConstants.Subscribe + ":" + userId;
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        for (Object key : keys) {
            String filed = (String) key;
            long expire = Long.parseLong((String) stringRedisTemplate.opsForHash().get(userKey, filed));
            if (expire > 0 && expire > System.currentTimeMillis()) {
                messageProducer.sendToUser(filed, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        pack, appId);
            } else {
                stringRedisTemplate.opsForHash().delete(userKey, filed);
            }
        }
    }

    @Override
    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req) {
        long subExpireTime = 0L;
        if (req != null && req.getSubTime() > 0) {
            subExpireTime = System.currentTimeMillis() + req.getSubTime();
        }

        for (String beSubUserId : req.getSubUserId()) {
            String userKey = req.getAppId() + ":" + Constants.RedisConstants.Subscribe + ":" + beSubUserId;
            stringRedisTemplate.opsForHash().put(userKey, req.getOperator(), Long.toString(subExpireTime));
        }
    }

}
