package com.pius.im.service.utils;

/**
 * @Author: Pius
 * @Date: 2024/1/1
 */
public class ConversationIdGenerate {

    //A|B
    //B A
    public static String generateP2PId(String fromId,String toId){
        int i = fromId.compareTo(toId);
        if(i < 0){
            return toId+"|"+fromId;
        }else if(i > 0){
            return fromId+"|"+toId;
        }

        throw new RuntimeException("不能给自己发消息");
    }
}
