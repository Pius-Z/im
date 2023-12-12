package com.pius.im.service.friendship.controller;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.friendship.dao.ImFriendShipEntity;
import com.pius.im.service.friendship.model.req.*;
import com.pius.im.service.friendship.model.resp.CheckFriendShipResp;
import com.pius.im.service.friendship.model.resp.ImportFriendShipResp;
import com.pius.im.service.friendship.service.ImFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@RestController
@RequestMapping("v1/friendship")
public class ImFriendShipController {

    @Autowired
    ImFriendService imFriendShipService;

    @RequestMapping("/importFriendShip")
    public ResponseVO<ImportFriendShipResp> importFriendShip(@RequestBody @Validated ImportFriendShipReq req) {
        return imFriendShipService.importFriendShip(req);
    }

    @RequestMapping("/addFriend")
    public ResponseVO addFriend(@RequestBody @Validated AddFriendReq req) {
        return imFriendShipService.addFriend(req);
    }

    @RequestMapping("/updateFriend")
    public ResponseVO updateFriend(@RequestBody @Validated UpdateFriendReq req) {
        return imFriendShipService.updateFriend(req);
    }

    @RequestMapping("/deleteFriend")
    public ResponseVO deleteFriend(@RequestBody @Validated DeleteFriendReq req) {
        return imFriendShipService.deleteFriend(req);
    }

    @RequestMapping("/deleteAllFriend")
    public ResponseVO deleteAllFriend(@RequestBody @Validated DeleteAllFriendReq req) {
        return imFriendShipService.deleteAllFriend(req);
    }

    @RequestMapping("/getAllFriendShip")
    public ResponseVO<List<ImFriendShipEntity>> getAllFriendShip(@RequestBody @Validated GetAllFriendShipReq req) {
        return imFriendShipService.getAllFriendShip(req);
    }

    @RequestMapping("/getRelation")
    public ResponseVO<ImFriendShipEntity> getRelation(@RequestBody @Validated GetRelationReq req) {
        return imFriendShipService.getRelation(req);
    }

    @RequestMapping("/checkFriendShip")
    public ResponseVO<List<CheckFriendShipResp>> checkFriendShip(@RequestBody @Validated CheckFriendShipReq req){
        return imFriendShipService.checkFriendShip(req);
    }

    @RequestMapping("/addBlack")
    public ResponseVO addBlack(@RequestBody @Validated AddFriendShipBlackReq req){
        return imFriendShipService.addBlack(req);
    }

    @RequestMapping("/deleteBlack")
    public ResponseVO deleteBlack(@RequestBody @Validated DeleteBlackReq req){
        return imFriendShipService.deleteBlack(req);
    }

    @RequestMapping("/checkBlack")
    public ResponseVO checkBlack(@RequestBody @Validated CheckFriendShipReq req){
        return imFriendShipService.checkBlack(req);
    }

}
