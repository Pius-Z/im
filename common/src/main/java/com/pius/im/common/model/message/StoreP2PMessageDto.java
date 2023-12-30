package com.pius.im.common.model.message;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/30
 */
@Data
public class StoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBody messageBody;

}
