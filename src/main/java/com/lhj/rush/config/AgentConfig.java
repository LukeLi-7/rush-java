package com.lhj.rush.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agentscope.agent")
public class AgentConfig {
    
    /**
     * Agent 名称
     */
    private String name = "quickstart-agent";
    
    /**
     * 系统提示词
     */
    private String sysPrompt = "你是一个帮助用户做笔记的助手。";
}
