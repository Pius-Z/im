package com.pius.im.service.friendship.model.req;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateFriendReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotNull(message = "toItem不能为空")
    private FriendDto toItem;
}
