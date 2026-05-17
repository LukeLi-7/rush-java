package com.lhj.rush.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    /**
     * 用户消息内容
     */
    private String message;
    
    /**
     * 会话 ID（可选，不传则自动生成）
     */
    private String sessionId;
    
    /**
     * 用户 ID（可选，不传则使用默认值）
     */
    private String userId;
}
