package com.pius.im.service.user.model.req;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteUserReq extends RequestBase {

    @NotEmpty(message = "用户id不能为空")
    private List<String> userId;

}
