package com.pius.im.service.user.controller;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.user.model.req.DeleteUserReq;
import com.pius.im.service.user.model.req.ImportUserReq;
import com.pius.im.service.user.model.req.LoginReq;
import com.pius.im.service.user.model.resp.ImportOrDeleteUserResp;
import com.pius.im.service.user.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@RestController
@RequestMapping("v1/user")
public class ImUserController {

    @Autowired
    ImUserService imUserService;

    @RequestMapping(value = "importUser", method = RequestMethod.POST)
    public ResponseVO<ImportOrDeleteUserResp> importUser(@RequestBody @Validated ImportUserReq req) {
        return imUserService.importUser(req);
    }

    @RequestMapping(value = "deleteUser", method = RequestMethod.POST)
    public ResponseVO<ImportOrDeleteUserResp> deleteUser(@RequestBody @Validated DeleteUserReq req) {
        return imUserService.deleteUser(req);
    }

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ResponseVO login(@RequestBody @Validated LoginReq req) {
        return imUserService.login(req);
    }

}
