package com.pius.im.common.constant;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
public class Constants {

    /**
     * channel绑定的userId Key
     */
    public static final String UserId = "userId";

    /**
     * channel绑定的appId
     */
    public static final String AppId = "appId";

    public static final String ClientType = "clientType";

    public static final String Imei = "imei";

    public static final String ReadTime = "readTime";


    public static class RedisConstants {

        /**
         * 用户session，appId + UserSessionConstants + 用户id 例如10000：userSession：lld
         */
        public static final String UserSessionConstants = ":userSession:";
    }
}
