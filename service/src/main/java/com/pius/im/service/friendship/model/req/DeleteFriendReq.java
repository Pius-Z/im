package com.pius.im.service.friendship.model.req;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2023/12/12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteFriendReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotBlank(message = "toId不能为空")
    private String toId;

}
