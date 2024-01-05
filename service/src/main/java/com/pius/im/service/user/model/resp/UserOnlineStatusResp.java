package com.pius.im.service.user.model.resp;

import com.pius.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2024/1/5
 */
@Data
public class UserOnlineStatusResp {

    private List<UserSession> session;

    private String customText;

    private Integer customStatus;

}
