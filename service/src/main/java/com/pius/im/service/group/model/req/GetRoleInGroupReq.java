package com.pius.im.service.group.model.req;

import com.pius.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetRoleInGroupReq extends RequestBase {

    private String groupId;

    private List<String> memberId;

}
