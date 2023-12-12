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
public class DeleteBlackReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    @NotBlank(message = "好友id不能为空")
    private String toId;

}
