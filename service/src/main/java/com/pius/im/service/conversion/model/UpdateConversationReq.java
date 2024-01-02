package com.pius.im.service.conversion.model;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2024/1/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateConversationReq extends RequestBase {

    @NotBlank(message = "会话id不能为空")
    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private String fromId;

}
