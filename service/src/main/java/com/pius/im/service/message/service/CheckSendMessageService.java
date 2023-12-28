package com.pius.im.service.message.service;

import com.pius.im.common.ResponseVO;
import com.pius.im.common.config.AppConfig;
import com.pius.im.common.enums.*;
import com.pius.im.service.friendship.dao.ImFriendShipEntity;
import com.pius.im.service.friendship.model.req.GetRelationReq;
import com.pius.im.service.friendship.service.ImFriendService;
import com.pius.im.service.group.dao.ImGroupEntity;
import com.pius.im.service.group.model.resp.GetRoleInGroupResp;
import com.pius.im.service.group.service.ImGroupMemberService;
import com.pius.im.service.group.service.ImGroupService;
import com.pius.im.service.user.dao.ImUserDataEntity;
import com.pius.im.service.user.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: Pius
 * @Date: 2023/12/27
 */
@Service
public class CheckSendMessageService {

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendService imFriendService;

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    AppConfig appConfig;

    public ResponseVO checkSenderForbiddenOrMute(String fromId, Integer appId) {

        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(fromId, appId);
        if (!singleUserInfo.isOk()) {
            return singleUserInfo;
        }

        ImUserDataEntity user = singleUserInfo.getData();
        if (user.getForbiddenFlag() == UserForbiddenTypeEnum.FORBIDDEN.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.FROM_IS_FORBIDDEN);
        } else if (user.getSilentFlag() == UserMuteTypeEnum.MUTE.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.FROM_IS_MUTE);
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO checkFriendShip(String fromId, String toId, Integer appId) {

        if (appConfig.isSendMessageCheckFriendship()) {
            GetRelationReq fromReq = new GetRelationReq();
            fromReq.setAppId(appId);
            fromReq.setFromId(fromId);
            fromReq.setToId(toId);
            ResponseVO<ImFriendShipEntity> fromRelation = imFriendService.getRelation(fromReq);
            if (!fromRelation.isOk()) {
                return fromRelation;
            }

            GetRelationReq toReq = new GetRelationReq();
            toReq.setAppId(appId);
            toReq.setFromId(toId);
            toReq.setToId(fromId);
            ResponseVO<ImFriendShipEntity> toRelation = imFriendService.getRelation(toReq);
            if (!toRelation.isOk()) {
                return toRelation;
            }

            if (fromRelation.getData().getStatus() != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            if (toRelation.getData().getStatus() != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_DELETED_YOU);
            }

            if (appConfig.isSendMessageCheckBlack()) {
                if (fromRelation.getData().getBlack() != FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
                }

                if (toRelation.getData().getBlack() != FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_BLACK_YOU);
                }
            }
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO checkGroupMessage(String fromId, String groupId, Integer appId) {

        ResponseVO responseVO = checkSenderForbiddenOrMute(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }

        // 判断群是否存在
        ResponseVO<ImGroupEntity> group = imGroupService.getGroupInfo(groupId, appId);
        if (!group.isOk()) {
            return group;
        }

        // 判断群成员是否在群内
        ResponseVO<GetRoleInGroupResp> roleInGroupOne = imGroupMemberService.getRoleInGroup(groupId, fromId, appId);
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }
        GetRoleInGroupResp data = roleInGroupOne.getData();

        // 判断群是否被禁言
        // 如果禁言 只有管理员和群主可以发言
        ImGroupEntity groupData = group.getData();
        if (groupData.getMute() == GroupMuteTypeEnum.MUTE.getCode()
                && data.getRole() != GroupMemberRoleEnum.MANAGER.getCode()
                && data.getRole() != GroupMemberRoleEnum.OWNER.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_GROUP_IS_MUTE);
        }

        if (data.getSpeakDate() != null && data.getSpeakDate() > System.currentTimeMillis()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_IS_SPEAK);
        }

        return ResponseVO.successResponse();
    }

}
