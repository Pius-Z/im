package com.pius.im.service.friendship.model.callback;

import com.pius.im.service.friendship.model.req.FriendDto;
import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/24
 */
@Data
public class AddFriendAfterCallbackDto {

    private String fromId;

    private FriendDto toItem;

}
