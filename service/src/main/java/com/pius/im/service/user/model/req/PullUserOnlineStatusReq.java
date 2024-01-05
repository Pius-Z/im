package com.pius.im.service.user.model.req;

import com.pius.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2024/1/5
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PullUserOnlineStatusReq extends RequestBase {

    private List<String> userList;

}
