package com.pius.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.pius.im.codec.pack.group.AddGroupMemberPack;
import com.pius.im.codec.pack.group.RemoveGroupMemberPack;
import com.pius.im.codec.pack.group.UpdateGroupMemberPack;
import com.pius.im.common.ClientType;
import com.pius.im.common.enums.command.Command;
import com.pius.im.common.enums.command.GroupEventCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.service.group.model.req.GroupMemberDto;
import com.pius.im.service.group.service.ImGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Component
public class GroupMessageProducer {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    public void producer(String userId, Command command, Object data, ClientInfo clientInfo) {
        JSONObject o = (JSONObject) JSONObject.toJSON(data);
        String groupId = o.getString("groupId");
        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());

        if (command.equals(GroupEventCommand.ADDED_MEMBER)) {
            // 发送给管理员和被加入人
            List<GroupMemberDto> groupManager = imGroupMemberService.getGroupManager(groupId, clientInfo.getAppId());
            AddGroupMemberPack addGroupMemberPack = o.toJavaObject(AddGroupMemberPack.class);
            List<String> members = addGroupMemberPack.getMembers();
            for (GroupMemberDto groupMemberDto : groupManager) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && groupMemberDto.getMemberId().equals(userId)) {
                    messageProducer.sendToUserExceptClient(groupMemberDto.getMemberId(), command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(groupMemberDto.getMemberId(), command, data, clientInfo.getAppId());
                }
            }
            for (String member : members) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)) {
                    messageProducer.sendToUserExceptClient(member, command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(member, command, data, clientInfo.getAppId());
                }
            }
        } else if (command.equals(GroupEventCommand.DELETED_MEMBER)) {
            // 发送给全体成员与被删除人
            List<String> members = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());
            RemoveGroupMemberPack removeGroupMemberPack = o.toJavaObject(RemoveGroupMemberPack.class);
            String member = removeGroupMemberPack.getMember();
            members.add(member);
            for (String memberId : members) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)) {
                    messageProducer.sendToUserExceptClient(memberId, command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(memberId, command, data, clientInfo.getAppId());
                }
            }
        } else if (command.equals(GroupEventCommand.UPDATED_MEMBER)) {
            // 发送给管理员与更新人
            List<GroupMemberDto> groupManager = imGroupMemberService.getGroupManager(groupId, clientInfo.getAppId());
            UpdateGroupMemberPack updateGroupMemberPack = o.toJavaObject(UpdateGroupMemberPack.class);
            String memberId = updateGroupMemberPack.getMemberId();
            GroupMemberDto groupMemberDto = new GroupMemberDto();
            groupMemberDto.setMemberId(memberId);
            groupManager.add(groupMemberDto);
            for (GroupMemberDto member : groupManager) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.getMemberId().equals(userId)) {
                    messageProducer.sendToUserExceptClient(member.getMemberId(), command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(member.getMemberId(), command, data, clientInfo.getAppId());
                }
            }
        } else {
            // 发送给所有人
            for (String memberId : groupMemberId) {
                if (clientInfo.getClientType() != null && clientInfo.getClientType() != ClientType.WEBAPI.getCode() && memberId.equals(userId)) {
                    messageProducer.sendToUserExceptClient(memberId, command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(memberId, command, data, clientInfo.getAppId());
                }
            }
        }
    }

}
