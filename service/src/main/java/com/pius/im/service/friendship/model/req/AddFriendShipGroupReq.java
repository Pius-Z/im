package com.pius.im.service.friendship.model.req;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AddFriendShipGroupReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    public String fromId;

    @NotBlank(message = "分组名称不能为空")
    private String groupName;

    private List<String> toIds;

}
