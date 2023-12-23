package com.pius.im.common.route;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/23
 */
@Data
@AllArgsConstructor
public final class RouteInfo {

    private String ip;

    private Integer port;

}