package com.lhj.rush.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agentscope.workspace")
public class WorkspaceConfig {
    
    /**
     * 工作区路径
     */
    private String path = ".agentscope/workspace";
}
