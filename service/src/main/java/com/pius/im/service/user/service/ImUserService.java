package com.pius.im.service.user.service;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.user.dao.ImUserDataEntity;
import com.pius.im.service.user.model.req.*;
import com.pius.im.service.user.model.resp.GetUserInfoResp;
import com.pius.im.service.user.model.resp.ImportOrDeleteUserResp;

import java.util.Map;

/**
 * @Author: Pius
 * @Date: 2023/12/8
 */
public interface ImUserService {

    ResponseVO<ImportOrDeleteUserResp> importUser(ImportUserReq req);

    ResponseVO<ImportOrDeleteUserResp> deleteUser(DeleteUserReq req);

    ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId , Integer appId);

    ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    ResponseVO modifyUserInfo(ModifyUserInfoReq req);

    ResponseVO login(LoginReq req);

    ResponseVO<Map<Object, Object>> getUserSequence(GetUserSequenceReq req);

}
