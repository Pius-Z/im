package com.pius.im.service.friendship.model.req;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteFriendShipGroupMemberReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotBlank(message = "分组名称不能为空")
    private String groupName;

    @NotEmpty(message = "请选择用户")
    private List<String> toIds;

}