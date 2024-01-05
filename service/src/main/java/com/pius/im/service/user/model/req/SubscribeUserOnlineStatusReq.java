package com.pius.im.service.user.model.req;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2024/1/5
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SubscribeUserOnlineStatusReq extends RequestBase {

    @NotEmpty(message = "订阅用户不能为空")
    private List<String> subUserId;

    private Long subTime;

}
