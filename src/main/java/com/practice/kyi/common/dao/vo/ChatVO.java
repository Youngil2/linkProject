package com.practice.kyi.common.dao.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatVO extends CommonVO {
    
    private static final long serialVersionUID = 1L;
    
    private String chatSeq;
    
    private String sessionId;
    
    private String message;
    
    private String response;
    
    private String messageType; // USER, BOT
    
    private String registDate;
    
    private String registUser;
    
    private String updateUser;
    
    private String updateDate;
    
    private String deleteYn;
    
    // OpenAI API 요청용 필드들
    private String model;
    
    private int maxTokens;
}