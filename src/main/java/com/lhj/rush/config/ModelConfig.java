package com.lhj.rush.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agentscope.model")
public class ModelConfig {
    
    /**
     * API 基础地址
     */
    private String baseUrl = "https://api.deepseek.com/v1";
    
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * 模型名称
     */
    private String modelName = "deepseek-chat";
    
    /**
     * 是否启用流式输出
     */
    private boolean stream = true;
}
