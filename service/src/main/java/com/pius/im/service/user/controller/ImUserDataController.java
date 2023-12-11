package com.pius.im.service.user.controller;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.user.dao.ImUserDataEntity;
import com.pius.im.service.user.model.req.GetUserInfoReq;
import com.pius.im.service.user.model.req.ModifyUserInfoReq;
import com.pius.im.service.user.model.req.GetSingleUserInfoReq;
import com.pius.im.service.user.model.resp.GetUserInfoResp;
import com.pius.im.service.user.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@RestController
@RequestMapping("v1/user/data")
public class ImUserDataController {

    @Autowired
    ImUserService imUserService;

    @RequestMapping(value = "/getSingleUserInfo", method = RequestMethod.GET)
    public ResponseVO<ImUserDataEntity> getSingleUserInfo(@RequestBody @Validated GetSingleUserInfoReq req) {
        return imUserService.getSingleUserInfo(req);
    }

    @RequestMapping(value = "/getUserInfo", method = RequestMethod.GET)
    public ResponseVO<GetUserInfoResp> getUserInfo(@RequestBody @Validated GetUserInfoReq req) {
        return imUserService.getUserInfo(req);
    }

    @RequestMapping(value = "/modifyUserInfo", method = RequestMethod.POST)
    public ResponseVO modifyUserInfo(@RequestBody @Validated ModifyUserInfoReq req) {
        return imUserService.modifyUserInfo(req);
    }

}
