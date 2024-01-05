package com.pius.im.codec.pack.user;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2024/1/5
 */
@Data
public class UserCustomStatusChangeNotifyPack {

    private String customText;

    private Integer customStatus;

    private String userId;

}
