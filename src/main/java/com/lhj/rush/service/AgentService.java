package com.lhj.rush.service;

import com.lhj.rush.config.*;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AgentService {
    
    private final ModelConfig modelConfig;
    private final AgentConfig agentConfig;
    private final WorkspaceConfig workspaceConfig;
    private final CompactionConfig compactionConfig;
    
    private HarnessAgent agent;
    private Path workspace;
    
    /**
     * 会话上下文缓存（生产环境建议使用 Redis）
     */
    private final Map<String, RuntimeContext> contextCache = new ConcurrentHashMap<>();
    
    public AgentService(ModelConfig modelConfig, AgentConfig agentConfig, 
                       WorkspaceConfig workspaceConfig, CompactionConfig compactionConfig) {
        this.modelConfig = modelConfig;
        this.agentConfig = agentConfig;
        this.workspaceConfig = workspaceConfig;
        this.compactionConfig = compactionConfig;
    }
    
    @PostConstruct
    public void init() throws Exception {
        log.info("初始化 AgentScope Agent...");
        
        // 1. 初始化工作区
        workspace = Paths.get(workspaceConfig.getPath());
        initWorkspaceIfAbsent(workspace);
        
        // 2. 构建模型
        Model model = OpenAIChatModel.builder()
                .baseUrl(modelConfig.getBaseUrl())
                .apiKey(modelConfig.getApiKey())
                .modelName(modelConfig.getModelName())
                .stream(modelConfig.isStream())
                .build();
        
        // 3. 构建 HarnessAgent
        agent = HarnessAgent.builder()
                .name(agentConfig.getName())
                .sysPrompt(agentConfig.getSysPrompt())
                .model(model)
                .workspace(workspace)
                .compaction(io.agentscope.harness.agent.memory.compaction.CompactionConfig.builder()
                        .triggerMessages(compactionConfig.getTriggerMessages())
                        .keepMessages(compactionConfig.getKeepMessages())
                        .flushBeforeCompact(compactionConfig.isFlushBeforeCompact())
                        .build())
                .build();
        
        log.info("AgentScope Agent 初始化完成");
    }
    
    /**
     * 发送消息并获取回复
     */
    public String sendMessage(String message, String sessionId, String userId) {
        try {
            // 获取或创建会话上下文
            RuntimeContext ctx = getOrCreateContext(sessionId, userId);
            
            // 构建用户消息
            Msg userMsg = Msg.builder()
                    .role(MsgRole.USER)
                    .textContent(message)
                    .build();
            
            // 调用 Agent
            Msg response = agent.call(userMsg, ctx).block();
            
            if (response != null) {
                return response.getTextContent();
            } else {
                return "抱歉，我没有收到回复。";
            }
        } catch (Exception e) {
            log.error("调用 Agent 失败", e);
            throw new RuntimeException("调用 Agent 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取或创建会话上下文
     */
    private RuntimeContext getOrCreateContext(String sessionId, String userId) {
        String key = sessionId != null ? sessionId : "default-session";
        
        return contextCache.computeIfAbsent(key, k -> {
            log.info("创建新会话: sessionId={}, userId={}", k, userId);
            return RuntimeContext.builder()
                    .sessionId(k)
                    .userId(userId != null ? userId : "default-user")
                    .build();
        });
    }
    
    /**
     * 初始化工作区
     */
    private void initWorkspaceIfAbsent(Path workspace) throws Exception {
        Files.createDirectories(workspace);
        Path agentsMd = workspace.resolve("AGENTS.md");
        if (Files.exists(agentsMd)) {
            log.info("工作区已存在: {}", workspace.toAbsolutePath());
            return;
        }
        
        Files.writeString(agentsMd, """
                # 笔记助手
                
                你是一个帮助用户整理笔记和知识的助手。
                
                ## 行为约定
                - 主动记录用户提到的关键事实(姓名、计划、偏好等)
                - 回答用简洁中文,必要时给出要点列表
                - 对不确定的内容要主动说明,不要臆造
                """);
        
        log.info("创建工作区配置文件: {}", agentsMd.toAbsolutePath());
    }
}
