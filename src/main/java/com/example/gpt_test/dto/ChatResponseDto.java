package com.example.gpt_test.dto;

/**
 * GPT 채팅 응답 DTO
 */
public class ChatResponseDto {
    
    private String answer;
    private String model;
    private long responseTime;
    
    public ChatResponseDto() {}
    
    public ChatResponseDto(String answer) {
        this.answer = answer;
    }
    
    public ChatResponseDto(String answer, String model, long responseTime) {
        this.answer = answer;
        this.model = model;
        this.responseTime = responseTime;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}
