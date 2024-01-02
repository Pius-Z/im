package com.pius.im.service.conversion.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pius.im.service.conversion.dao.ImConversationSetEntity;
import org.apache.ibatis.annotations.Update;

/**
 * @Author: Pius
 * @Date: 2024/1/2
 */
public interface ImConversationSetMapper extends BaseMapper<ImConversationSetEntity> {

    @Update(" update im_conversation_set set read_sequence = #{readSequence}, sequence = #{sequence} " +
            " where conversation_id = #{conversationId} and app_id = #{appId} AND read_sequence < #{readSequence}")
    void readMark(ImConversationSetEntity imConversationSetEntity);

}
