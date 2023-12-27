package com.pius.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.pius.im.common.BaseErrorCode;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.enums.SignErrorCode;
import com.pius.im.common.exception.ApplicationExceptionEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.Arrays;

/**
 * @Author: Pius
 * @Date: 2023/12/27
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    IdentityCheck identityCheck;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (1 == 1) {
            return true;
        }

        // 获取appId 操作人 userSign
        String appId = request.getParameter("appId");
        if (StringUtils.isBlank(appId)) {
            resp(ResponseVO.errorResponse(SignErrorCode.APPID_NOT_EXIST), response);
            return false;
        }

        String identifier = request.getParameter("identifier");
        if (StringUtils.isBlank(identifier)) {
            resp(ResponseVO.errorResponse(SignErrorCode.OPERATOR_NOT_EXIST), response);
            return false;
        }

        String userSign = request.getParameter("userSign");
        if (StringUtils.isBlank(userSign)) {
            resp(ResponseVO.errorResponse(SignErrorCode.USER_SIGN_NOT_EXIST), response);
            return false;
        }

        // 签名是否匹配
        ApplicationExceptionEnum applicationExceptionEnum = identityCheck.checkUserSign(identifier, appId, userSign);
        if (applicationExceptionEnum != BaseErrorCode.SUCCESS) {
            resp(ResponseVO.errorResponse(applicationExceptionEnum), response);
            return false;
        }

        return true;
    }

    private void resp(ResponseVO respVo, HttpServletResponse response) {

        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            String resp = JSONObject.toJSONString(respVo);
            writer = response.getWriter();
            writer.write(resp);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }
}
