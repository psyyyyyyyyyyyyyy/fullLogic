package com.example.gpt_test.controller;

import com.example.gpt_test.dto.ChatRequestDto;
import com.example.gpt_test.dto.ChatResponseDto;
import com.example.gpt_test.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GPT 채팅 API 컨트롤러
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatService chatService;
    
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    /**
     * GPT에게 질문하고 답변을 받는 API
     * 
     * @param request 사용자의 질문이 포함된 요청 객체
     * @return GPT의 답변이 포함된 응답 객체
     */
    @PostMapping("/ask")
    public ResponseEntity<ChatResponseDto> askQuestion(@RequestBody ChatRequestDto request) {
        
        String shortQuestion = request.getQuestion().length() > 50 
            ? request.getQuestion().substring(0, 50) + "..."
            : request.getQuestion();
        String modelUsed = request.getModel() != null ? request.getModel() : "기본 모델";
        logger.info("GPT 질문 요청 - 모델: {}, 질문: {}", modelUsed, shortQuestion);
        
        ChatResponseDto response = chatService.getChatResponse(request);
        
        logger.info("GPT 질문 응답 완료 - 모델: {}, 질문 길이: {}, 응답 길이: {}", 
                   modelUsed, request.getQuestion().length(), response.getAnswer().length());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 서비스 상태 확인 API
     * 
     * @return 서비스 상태 정보
     */
    @GetMapping("/health")
    public ResponseEntity<String> checkHealth() {
        logger.info("서비스 상태 확인 요청");
        return ResponseEntity.ok("{\"status\": \"OK\", \"service\": \"GPT Chat Service\", \"timestamp\": " + System.currentTimeMillis() + "}");
    }
}
