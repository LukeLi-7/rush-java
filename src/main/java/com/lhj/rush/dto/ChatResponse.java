package com.lhj.rush.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    /**
     * AI 回复内容
     */
    private String reply;
    
    /**
     * 会话 ID
     */
    private String sessionId;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息（失败时）
     */
    private String error;
}
