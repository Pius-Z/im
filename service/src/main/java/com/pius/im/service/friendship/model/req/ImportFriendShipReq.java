package com.pius.im.service.friendship.model.req;

import com.pius.im.common.enums.FriendShipStatusEnum;
import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ImportFriendShipReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;

    private List<ImportFriendDto> friendItem;

    @Data
    public static class ImportFriendDto {

        private String toId;

        private String remark;

        private String addSource;

        private Integer status = FriendShipStatusEnum.FRIEND_STATUS_NO_FRIEND.getCode();

        private Integer black = FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode();
    }

}
