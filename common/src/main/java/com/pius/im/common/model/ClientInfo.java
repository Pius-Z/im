package com.pius.im.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Pius
 * @Date: 2023/12/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {

    private Integer appId;

    private Integer clientType;

    private String imei;

}
