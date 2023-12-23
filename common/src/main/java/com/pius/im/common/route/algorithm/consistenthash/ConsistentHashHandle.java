package com.pius.im.common.route.algorithm.consistenthash;

import com.pius.im.common.route.RouteHandle;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/23
 */
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConsistentHashHandle implements RouteHandle {

    private AbstractConsistentHash hash;

    @Override
    public String routeServer(List<String> values, String key) {
        return hash.process(values, key);
    }

}
