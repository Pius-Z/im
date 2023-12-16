package com.pius.im.service.group.model.req;

import com.pius.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetGroupAndMemberReq extends RequestBase {

    private String groupId;

}
