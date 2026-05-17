package com.lhj.rush.controller;

import com.lhj.rush.dto.ChatRequest;
import com.lhj.rush.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Slf4j
@Controller
public class ChatController {
    
    private final AgentService agentService;
    
    public ChatController(AgentService agentService) {
        this.agentService = agentService;
    }
    
    /**
     * 首页 - 聊天页面
     */
    @GetMapping("/")
    public String index() {
        return "chat";
    }
    
    /**
     * WebFlux SSE 流式聊天接口
     * 使用 Flux + ServerSentEvent 实现响应式流，更加优雅和高效
     */
    @PostMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<ServerSentEvent<String>> chatStream(@RequestBody ChatRequest request) {
        // 生成会话 ID（如果未提供）
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        final String finalSessionId = sessionId;
        
        String userId = request.getUserId();
        if (userId == null || userId.isEmpty()) {
            userId = "user-" + sessionId.substring(0, 8);
        }
        final String finalUserId = userId;
        
        log.info("收到聊天请求: sessionId={}, userId={}", finalSessionId, finalUserId);
        
        return Flux.just(
                ServerSentEvent.<String>builder()
                    .event("session")
                    .data(finalSessionId)
                    .build(),
                ServerSentEvent.<String>builder()
                    .event("start")
                    .data("开始处理...")
                    .build()
            )
            .concatWith(
                Flux.defer(() -> {
                    try {
                        String reply = agentService.sendMessage(
                            request.getMessage(), 
                            finalSessionId, 
                            finalUserId
                        );
                        
                        String[] chunks = reply.split("(?<=\\.|。|!|！|\n)");
                        
                        return Flux.fromArray(chunks)
                            .filter(chunk -> !chunk.trim().isEmpty())
                            .map(chunk -> ServerSentEvent.<String>builder()
                                .event("message")
                                .data(chunk.trim())
                                .build())
                            .delayElements(java.time.Duration.ofMillis(100));
                            
                    } catch (Exception e) {
                        log.error("调用 Agent 失败", e);
                        return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data("错误: " + e.getMessage())
                            .build());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
            )
            .concatWith(
                Flux.just(ServerSentEvent.<String>builder()
                    .event("complete")
                    .data("完成")
                    .build())
            )
            .doOnSubscribe(s -> log.info("SSE 连接建立: sessionId={}", finalSessionId))
            .doOnComplete(() -> log.info("SSE 连接完成: sessionId={}", finalSessionId))
            .doOnError(e -> log.error("SSE 连接错误: sessionId={}", finalSessionId, e));
    }
}
