package com.pius.im.service.user.service;

import com.pius.im.service.user.model.UserStatusChangeNotifyContent;

/**
 * @Author: Pius
 * @Date: 2024/1/4
 */
public interface ImUserStatusService {

    void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

}
