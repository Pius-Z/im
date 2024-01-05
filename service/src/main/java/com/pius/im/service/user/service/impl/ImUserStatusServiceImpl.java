package com.pius.im.service.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pius.im.codec.pack.user.UserCustomStatusChangeNotifyPack;
import com.pius.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.command.Command;
import com.pius.im.common.enums.command.UserEventCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.common.model.RequestBase;
import com.pius.im.common.model.UserSession;
import com.pius.im.service.friendship.service.ImFriendService;
import com.pius.im.service.user.model.UserStatusChangeNotifyContent;
import com.pius.im.service.user.model.req.PullUserOnlineStatusReq;
import com.pius.im.service.user.model.req.SetUserCustomStatusReq;
import com.pius.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.pius.im.service.user.model.resp.UserOnlineStatusResp;
import com.pius.im.service.user.service.ImUserStatusService;
import com.pius.im.service.utils.MessageProducer;
import com.pius.im.service.utils.UserSessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        syncSender(userStatusChangeNotifyPack, content.getUserId(), content,
                UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC);

        dispatcher(userStatusChangeNotifyPack, content.getUserId(), content.getAppId(),
                UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY);
    }

    private void syncSender(Object pack, String userId, ClientInfo clientInfo, Command command) {
        messageProducer.sendToUserExceptClient(userId, command, pack, clientInfo);
    }

    private void dispatcher(Object pack, String userId, Integer appId, Command command) {
        List<String> allFriendId = imFriendService.getAllFriendId(userId, appId);
        for (String friendId : allFriendId) {
            messageProducer.sendToUser(friendId, command, pack, appId);
        }

        String userKey = appId + ":" + Constants.RedisConstants.Subscribe + ":" + userId;
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        for (Object key : keys) {
            String filed = (String) key;
            long expire = Long.parseLong((String) stringRedisTemplate.opsForHash().get(userKey, filed));
            if (expire > 0 && expire > System.currentTimeMillis()) {
                messageProducer.sendToUser(filed, command, pack, appId);
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

    @Override
    public void setUserCustomStatus(SetUserCustomStatusReq req) {
        UserCustomStatusChangeNotifyPack userCustomStatusChangeNotifyPack = new UserCustomStatusChangeNotifyPack();
        userCustomStatusChangeNotifyPack.setCustomStatus(req.getCustomStatus());
        userCustomStatusChangeNotifyPack.setCustomText(req.getCustomText());
        userCustomStatusChangeNotifyPack.setUserId(req.getUserId());
        String key = req.getAppId() + ":" + Constants.RedisConstants.UserCustomStatus + ":" + req.getUserId();
        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(userCustomStatusChangeNotifyPack));

        syncSender(userCustomStatusChangeNotifyPack, req.getUserId(),
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()),
                UserEventCommand.USER_CUSTOM_STATUS_CHANGE_NOTIFY_SYNC);
        dispatcher(userCustomStatusChangeNotifyPack, req.getUserId(), req.getAppId(),
                UserEventCommand.USER_CUSTOM_STATUS_CHANGE_NOTIFY);
    }

    @Override
    public Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(RequestBase req) {
        List<String> allFriendId = imFriendService.getAllFriendId(req.getOperator(), req.getAppId());
        return getUserOnlineStatus(allFriendId, req.getAppId());
    }

    @Override
    public Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req) {
        return getUserOnlineStatus(req.getUserList(), req.getAppId());
    }

    private Map<String, UserOnlineStatusResp> getUserOnlineStatus(List<String> userId, Integer appId) {
        Map<String, UserOnlineStatusResp> result = new HashMap<>(userId.size());
        for (String uid : userId) {
            UserOnlineStatusResp resp = new UserOnlineStatusResp();
            List<UserSession> userSession = userSessionUtils.getUserSession(appId, uid);
            resp.setSession(userSession);
            String key = appId + ":" + Constants.RedisConstants.UserCustomStatus + ":" + uid;
            String status = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(status)) {
                JSONObject parse = (JSONObject) JSON.parse(status);
                resp.setCustomText(parse.getString("customText"));
                resp.setCustomStatus(parse.getInteger("customStatus"));
            }
            result.put(uid, resp);
        }
        return result;
    }


}
