package com.example.gpt_test.dto;

import java.util.List;

/**
 * 인물 식별 결과 응답 DTO
 */
public class PersonIdentificationResponseDto {
    
    private String imageUrl;
    private List<String> extractedTitles;
    private List<String> extractedQueries;
    private String gptAnalysis;
    private String identifiedPerson;
    private String identifiedGroup;
    private boolean isMatch;          // 사용자 선호와 일치하는지 여부
    private String matchReason;       // 일치/불일치 이유
    private long processingTime;
    
    public PersonIdentificationResponseDto() {}
    
    public PersonIdentificationResponseDto(String imageUrl, List<String> extractedTitles, 
                                         List<String> extractedQueries, String gptAnalysis, 
                                         String identifiedPerson, String identifiedGroup,
                                         boolean isMatch, String matchReason, long processingTime) {
        this.imageUrl = imageUrl;
        this.extractedTitles = extractedTitles;
        this.extractedQueries = extractedQueries;
        this.gptAnalysis = gptAnalysis;
        this.identifiedPerson = identifiedPerson;
        this.identifiedGroup = identifiedGroup;
        this.isMatch = isMatch;
        this.matchReason = matchReason;
        this.processingTime = processingTime;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public List<String> getExtractedTitles() {
        return extractedTitles;
    }
    
    public void setExtractedTitles(List<String> extractedTitles) {
        this.extractedTitles = extractedTitles;
    }
    
    public List<String> getExtractedQueries() {
        return extractedQueries;
    }
    
    public void setExtractedQueries(List<String> extractedQueries) {
        this.extractedQueries = extractedQueries;
    }
    
    public String getGptAnalysis() {
        return gptAnalysis;
    }
    
    public void setGptAnalysis(String gptAnalysis) {
        this.gptAnalysis = gptAnalysis;
    }
    
    public String getIdentifiedPerson() {
        return identifiedPerson;
    }
    
    public void setIdentifiedPerson(String identifiedPerson) {
        this.identifiedPerson = identifiedPerson;
    }
    
    public long getProcessingTime() {
        return processingTime;
    }
    
    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }
    
    public String getIdentifiedGroup() {
        return identifiedGroup;
    }
    
    public void setIdentifiedGroup(String identifiedGroup) {
        this.identifiedGroup = identifiedGroup;
    }
    
    public boolean isMatch() {
        return isMatch;
    }
    
    public void setMatch(boolean match) {
        isMatch = match;
    }
    
    public String getMatchReason() {
        return matchReason;
    }
    
    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }
}
