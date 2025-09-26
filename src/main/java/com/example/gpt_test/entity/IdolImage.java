package com.example.gpt_test.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 아이돌 이미지 엔티티
 */
@Entity
@Table(name = "idol_images")
public class IdolImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String idolName;
    
    @Column(nullable = false)
    private String groupName;
    
    @Column(nullable = false)
    private String imageUrl;
    
    @Column(nullable = false, unique = true)
    private String pHash;
    
    @Column(nullable = false)
    private String originalFileName;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt;
    
    @Column(columnDefinition = "TEXT")
    private String gptAnalysis;
    
    @Column
    private Boolean isVerified;
    
    public IdolImage() {}
    
    public IdolImage(String idolName, String groupName, String imageUrl, String pHash, 
                     String originalFileName, Long fileSize, String contentType) {
        this.idolName = idolName;
        this.groupName = groupName;
        this.imageUrl = imageUrl;
        this.pHash = pHash;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.uploadedAt = LocalDateTime.now();
        this.isVerified = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getIdolName() {
        return idolName;
    }
    
    public void setIdolName(String idolName) {
        this.idolName = idolName;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    public String getGptAnalysis() {
        return gptAnalysis;
    }
    
    public void setGptAnalysis(String gptAnalysis) {
        this.gptAnalysis = gptAnalysis;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }
}
