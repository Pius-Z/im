package com.pius.im.service.user.service;

import com.pius.im.common.model.RequestBase;
import com.pius.im.service.user.model.UserStatusChangeNotifyContent;
import com.pius.im.service.user.model.req.PullUserOnlineStatusReq;
import com.pius.im.service.user.model.req.SetUserCustomStatusReq;
import com.pius.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.pius.im.service.user.model.resp.UserOnlineStatusResp;

import java.util.Map;

/**
 * @Author: Pius
 * @Date: 2024/1/4
 */
public interface ImUserStatusService {

    void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    void setUserCustomStatus(SetUserCustomStatusReq req);

    Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(RequestBase req);

    Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req);

}
