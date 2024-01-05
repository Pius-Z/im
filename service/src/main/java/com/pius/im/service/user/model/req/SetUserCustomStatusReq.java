package com.pius.im.service.user.model.req;

import com.pius.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2024/1/5
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SetUserCustomStatusReq extends RequestBase {

    private String userId;

    private String customText;

    private Integer customStatus;

}
