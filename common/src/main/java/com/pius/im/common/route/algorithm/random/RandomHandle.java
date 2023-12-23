package com.pius.im.common.route.algorithm.random;

import com.pius.im.common.enums.UserErrorCode;
import com.pius.im.common.exception.ApplicationException;
import com.pius.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: Pius
 * @Date: 2023/12/23
 */
public class RandomHandle implements RouteHandle {

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }

        return values.get(ThreadLocalRandom.current().nextInt(size));
    }

}
