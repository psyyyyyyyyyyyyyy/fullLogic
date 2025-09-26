package com.example.gpt_test.dto;

/**
 * 사용자의 채팅 요청 DTO
 */
public class ChatRequestDto {
    
    private String question;
    private String model; // 사용할 GPT 모델명
    
    public ChatRequestDto() {}
    
    public ChatRequestDto(String question) {
        this.question = question;
    }
    
    public ChatRequestDto(String question, String model) {
        this.question = question;
        this.model = model;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
}
