package com.pius.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.pius.im.common.BaseErrorCode;
import com.pius.im.common.config.AppConfig;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.SignErrorCode;
import com.pius.im.common.exception.ApplicationExceptionEnum;
import com.pius.im.common.utils.SignAPI;
import com.pius.im.service.user.service.ImUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Pius
 * @Date: 2023/12/27
 */
@Slf4j
@Component
public class IdentityCheck {

    @Autowired
    ImUserService imUserService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public ApplicationExceptionEnum checkUserSign(String identifier, String appId, String userSign) {
        // 从缓存中找
        String cacheUserSign = stringRedisTemplate.opsForValue().get(appId + ":" +
                Constants.RedisConstants.UserSign + ":" + identifier + userSign);
        if (!StringUtils.isBlank(cacheUserSign) && Long.parseLong(cacheUserSign) > System.currentTimeMillis() / 1000) {
            return BaseErrorCode.SUCCESS;
        }

        // 获取秘钥
        String privateKey = appConfig.getPrivateKey();

        // 根据appid + 秘钥创建signApi
        SignAPI signAPI = new SignAPI(Long.parseLong(appId), privateKey);

        // 调用sigApi对userSig解密
        JSONObject jsonObject = SignAPI.decodeUserSign(userSign);

        // 取出解密后的appid 和 identifier 和 过期时间等做匹配，不通过则提示错误
        String decoerAppId = "";
        String decoderIdentifier = "";
        long decoderExpireTime = 0L;
        long decoderExpire = 0L;
        String decoderSign = "";

        try {
            decoerAppId = jsonObject.getString("TLS.appId");
            decoderIdentifier = jsonObject.getString("TLS.identifier");
            decoderExpire = Long.parseLong(jsonObject.get("TLS.expire").toString());
            decoderExpireTime = Long.parseLong(jsonObject.get("TLS.expireTime").toString());
            decoderSign =  jsonObject.getString("TLS.sign");
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }

        if (!decoerAppId.equals(appId)) {
            return SignErrorCode.USER_SIGN_IS_ERROR;
        }

        if (!decoderIdentifier.equals(identifier)) {
            return SignErrorCode.USER_SIGN_OPERATE_NOT_MATE;
        }


        if (decoderExpireTime < System.currentTimeMillis() / 1000) {
            return SignErrorCode.USER_SIGN_IS_EXPIRED;
        }

        // 生成sign与请求的sign对比
        String sign = signAPI.hmacsha256(decoderIdentifier, decoderExpire, decoderExpireTime, null);
        if (!sign.equals(decoderSign)) {
            return SignErrorCode.USER_SIGN_IS_ERROR;
        }

        String key = appId + ":" + Constants.RedisConstants.UserSign + ":" + identifier + userSign;

        long etime = decoderExpireTime - System.currentTimeMillis() / 1000;

        stringRedisTemplate.opsForValue().set(key, Long.toString(decoderExpireTime), etime, TimeUnit.SECONDS);

        return BaseErrorCode.SUCCESS;
    }

}
