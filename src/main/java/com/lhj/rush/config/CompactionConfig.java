package com.lhj.rush.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agentscope.compaction")
public class CompactionConfig {
    
    /**
     * 触发压缩的消息数量阈值
     */
    private int triggerMessages = 30;
    
    /**
     * 压缩后保留的消息数量
     */
    private int keepMessages = 10;
    
    /**
     * 压缩前是否刷新事实到日流水账
     */
    private boolean flushBeforeCompact = true;
}
