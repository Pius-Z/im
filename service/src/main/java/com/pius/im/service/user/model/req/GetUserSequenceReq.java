package com.pius.im.service.user.model.req;

import com.pius.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2024/1/3
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetUserSequenceReq extends RequestBase {

    private String userId;

}
