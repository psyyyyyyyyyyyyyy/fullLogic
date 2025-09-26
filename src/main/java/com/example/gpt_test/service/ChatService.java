package com.example.gpt_test.service;

import com.example.gpt_test.dto.ChatRequestDto;
import com.example.gpt_test.dto.ChatResponseDto;
import com.example.gpt_test.dto.openai.OpenAiRequestDto;
import com.example.gpt_test.dto.openai.OpenAiResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * GPT 채팅 서비스
 */
@Service
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.api.url}")
    private String apiUrl;
    
    @Value("${openai.api.model}")
    private String defaultModel;
    
    private final RestTemplate restTemplate;
    
    public ChatService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * GPT에게 질문하고 답변을 받는 메서드
     */
    public ChatResponseDto getChatResponse(ChatRequestDto request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 사용할 모델 결정
            String model = request.getModel() != null ? request.getModel() : defaultModel;
            
            // OpenAI API 요청 객체 생성
            OpenAiRequestDto.Message message = new OpenAiRequestDto.Message("user", request.getQuestion());
            OpenAiRequestDto openAiRequest = new OpenAiRequestDto(
                model,
                Arrays.asList(message),
                0.7   // temperature
            );
            openAiRequest.setMaxTokens(1000);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<OpenAiRequestDto> entity = new HttpEntity<>(openAiRequest, headers);
            
            // OpenAI API 호출
            ResponseEntity<OpenAiResponseDto> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                OpenAiResponseDto.class
            );
            
            // 응답 처리
            OpenAiResponseDto openAiResponse = response.getBody();
            if (openAiResponse != null && openAiResponse.getChoices() != null && !openAiResponse.getChoices().isEmpty()) {
                String answer = openAiResponse.getChoices().get(0).getMessage().getContent();
                long responseTime = System.currentTimeMillis() - startTime;
                
                return new ChatResponseDto(answer, model, responseTime);
            } else {
                throw new RuntimeException("OpenAI API에서 유효한 응답을 받지 못했습니다.");
            }
            
        } catch (Exception e) {
            logger.error("GPT API 호출 중 오류 발생: {}", e.getMessage(), e);
            long responseTime = System.currentTimeMillis() - startTime;
            return new ChatResponseDto("죄송합니다. 요청을 처리하는 중 오류가 발생했습니다: " + e.getMessage(), 
                                     request.getModel(), responseTime);
        }
    }
}
