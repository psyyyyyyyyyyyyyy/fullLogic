package com.example.gpt_test.dto;

import com.example.gpt_test.entity.IdolImage;
import java.util.List;

/**
 * 아이돌 이미지 업로드 응답 DTO
 */
public class IdolUploadResponseDto {
    
    private boolean success;
    private String message;
    private List<ImageAnalysisResult> results;
    private long totalProcessingTime;
    private List<IdolImage> existingImages;
    
    public IdolUploadResponseDto() {}
    
    public IdolUploadResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public IdolUploadResponseDto(boolean success, String message, List<ImageAnalysisResult> results, long totalProcessingTime) {
        this.success = success;
        this.message = message;
        this.results = results;
        this.totalProcessingTime = totalProcessingTime;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<ImageAnalysisResult> getResults() {
        return results;
    }
    
    public void setResults(List<ImageAnalysisResult> results) {
        this.results = results;
    }
    
    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }
    
    public void setTotalProcessingTime(long totalProcessingTime) {
        this.totalProcessingTime = totalProcessingTime;
    }
    
    public List<IdolImage> getExistingImages() {
        return existingImages;
    }
    
    public void setExistingImages(List<IdolImage> existingImages) {
        this.existingImages = existingImages;
    }
    
    /**
     * 개별 이미지 분석 결과
     */
    public static class ImageAnalysisResult {
        private String fileName;
        private String imageUrl;
        private String pHash;
        private boolean match;
        private String matchReason;
        private String identifiedPerson;
        private String identifiedGroup;
        private String gptAnalysis;
        private long processingTime;
        
        public ImageAnalysisResult() {}
        
        public ImageAnalysisResult(String fileName, String imageUrl, String pHash, boolean match, 
                                 String matchReason, String identifiedPerson, String identifiedGroup, 
                                 String gptAnalysis, long processingTime) {
            this.fileName = fileName;
            this.imageUrl = imageUrl;
            this.pHash = pHash;
            this.match = match;
            this.matchReason = matchReason;
            this.identifiedPerson = identifiedPerson;
            this.identifiedGroup = identifiedGroup;
            this.gptAnalysis = gptAnalysis;
            this.processingTime = processingTime;
        }
        
        // Getters and Setters
        public String getFileName() {
            return fileName;
        }
        
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public String getpHash() {
            return pHash;
        }
        
        public void setpHash(String pHash) {
            this.pHash = pHash;
        }
        
        public boolean isMatch() {
            return match;
        }
        
        public void setMatch(boolean match) {
            this.match = match;
        }
        
        public String getMatchReason() {
            return matchReason;
        }
        
        public void setMatchReason(String matchReason) {
            this.matchReason = matchReason;
        }
        
        public String getIdentifiedPerson() {
            return identifiedPerson;
        }
        
        public void setIdentifiedPerson(String identifiedPerson) {
            this.identifiedPerson = identifiedPerson;
        }
        
        public String getIdentifiedGroup() {
            return identifiedGroup;
        }
        
        public void setIdentifiedGroup(String identifiedGroup) {
            this.identifiedGroup = identifiedGroup;
        }
        
        public String getGptAnalysis() {
            return gptAnalysis;
        }
        
        public void setGptAnalysis(String gptAnalysis) {
            this.gptAnalysis = gptAnalysis;
        }
        
        public long getProcessingTime() {
            return processingTime;
        }
        
        public void setProcessingTime(long processingTime) {
            this.processingTime = processingTime;
        }
    }
}
