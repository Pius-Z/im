package com.pius.im.common.utils;

import com.pius.im.common.BaseErrorCode;
import com.pius.im.common.exception.ApplicationException;
import com.pius.im.common.route.RouteInfo;

/**
 * @Author: Pius
 * @Date: 2023/12/23
 */
public class RouteInfoParseUtil {

    public static RouteInfo parse(String info) {
        try {
            String[] serverInfo = info.split(":");
            return new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1]));
        } catch (Exception e) {
            throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR);
        }
    }
}
