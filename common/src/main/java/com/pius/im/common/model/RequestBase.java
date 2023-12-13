package com.pius.im.common.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/10
 */
@Data
public class RequestBase {

    @NotNull(message = "appId不能为空")
    private Integer appId;

    private String operator;

}
