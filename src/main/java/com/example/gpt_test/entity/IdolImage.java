package com.example.gpt_test.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 아이돌 이미지 엔티티 (사용자별 개인 갤러리 + 그룹별 공유 DB)
 */
@Entity
@Table(name = "idol_images")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    
    @Column(nullable = false)
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
    
    // 사용자 관계 (개인 갤러리용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
    private User user;
    
    // 그룹_아이돌 관계 (공유 DB용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_idol_id", nullable = false)
    @JsonBackReference("groupIdol-images")
    private GroupIdol groupIdol;
    
    @Column(nullable = false)
    private Boolean isInPersonalGallery = true; // 개인 갤러리에 포함 여부
    
    @Column(nullable = false)
    private Boolean isInGroupDatabase = false; // 그룹 DB에 포함 여부 (GPT 일치 시에만 true)
    
    public IdolImage() {}
    
    public IdolImage(String idolName, String groupName, String imageUrl, String pHash, 
                     String originalFileName, Long fileSize, String contentType, User user, GroupIdol groupIdol) {
        this.idolName = idolName;
        this.groupName = groupName;
        this.imageUrl = imageUrl;
        this.pHash = pHash;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.user = user;
        this.groupIdol = groupIdol;
        this.uploadedAt = LocalDateTime.now();
        this.isVerified = false;
        this.isInPersonalGallery = true;
        this.isInGroupDatabase = false;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public GroupIdol getGroupIdol() {
        return groupIdol;
    }
    
    public void setGroupIdol(GroupIdol groupIdol) {
        this.groupIdol = groupIdol;
    }
    
    public Boolean getIsInPersonalGallery() {
        return isInPersonalGallery;
    }
    
    public void setIsInPersonalGallery(Boolean isInPersonalGallery) {
        this.isInPersonalGallery = isInPersonalGallery;
    }
    
    public Boolean getIsInGroupDatabase() {
        return isInGroupDatabase;
    }
    
    public void setIsInGroupDatabase(Boolean isInGroupDatabase) {
        this.isInGroupDatabase = isInGroupDatabase;
    }
    
    /**
     * GPT 분석 결과가 일치할 때 그룹 DB에 추가
     */
    public void addToGroupDatabase() {
        this.isInGroupDatabase = true;
        this.isVerified = true;
    }
}
