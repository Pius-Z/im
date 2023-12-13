package com.pius.im.service.friendship.model.req;

import com.pius.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApproveFriendRequestReq extends RequestBase {

    private Long id;

    /**
     * 1同意
     * 2拒绝
     */
    private Integer status;

}
