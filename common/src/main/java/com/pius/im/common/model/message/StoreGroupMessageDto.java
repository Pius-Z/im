package com.pius.im.common.model.message;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/30
 */
@Data
public class StoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBody messageBody;

}
