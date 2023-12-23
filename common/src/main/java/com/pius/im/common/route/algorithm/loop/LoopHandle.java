package com.pius.im.common.route.algorithm.loop;

import com.pius.im.common.enums.UserErrorCode;
import com.pius.im.common.exception.ApplicationException;
import com.pius.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: Pius
 * @Date: 2023/12/23
 */
public class LoopHandle implements RouteHandle {

    private AtomicLong index = new AtomicLong();

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }

        Long l = index.incrementAndGet() % size;
        if (l < 0) {
            // 溢出为负数的处理
            l = 0L;
        }

        return values.get(l.intValue());
    }

}
