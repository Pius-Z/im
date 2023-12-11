package com.pius.im.service.user.model.req;

import com.pius.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetUserInfoReq extends RequestBase {

    private List<String> userIds;

}
