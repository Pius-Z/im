package com.pius.im.common.model;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
@Data
public class UserClientDto {

    private Integer appId;

    private Integer clientType;

    private String userId;

    private String imei;

}
