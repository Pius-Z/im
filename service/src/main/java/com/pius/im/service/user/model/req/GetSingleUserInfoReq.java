package com.pius.im.service.user.model.req;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetSingleUserInfoReq extends RequestBase {

    @NotNull(message = "用户id不能为空")
    private String userId;

}
