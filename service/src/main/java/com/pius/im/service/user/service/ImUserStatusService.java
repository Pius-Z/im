package com.pius.im.service.user.service;

import com.pius.im.service.user.model.UserStatusChangeNotifyContent;
import com.pius.im.service.user.model.req.SetUserCustomStatusReq;
import com.pius.im.service.user.model.req.SubscribeUserOnlineStatusReq;

/**
 * @Author: Pius
 * @Date: 2024/1/4
 */
public interface ImUserStatusService {

    void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    void setUserCustomStatus(SetUserCustomStatusReq req);

}
