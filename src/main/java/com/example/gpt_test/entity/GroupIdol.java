package com.example.gpt_test.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 그룹_아이돌 엔티티 (그룹과 아이돌명의 조합으로 고유한 값)
 */
@Entity
@Table(name = "group_idols", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"groupName", "idolName"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GroupIdol {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String groupName;
    
    @Column(nullable = false)
    private String idolName;
    
    @Column(nullable = false, unique = true)
    private String groupIdolKey; // "그룹명_아이돌명" 형태의 고유 키
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private Integer imageCount = 0; // 해당 그룹_아이돌의 총 이미지 수
    
    @OneToMany(mappedBy = "groupIdol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("groupIdol-images")
    private List<IdolImage> images = new ArrayList<>();
    
    public GroupIdol() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public GroupIdol(String groupName, String idolName) {
        this();
        this.groupName = groupName;
        this.idolName = idolName;
        this.groupIdolKey = generateGroupIdolKey(groupName, idolName);
    }
    
    /**
     * 그룹명과 아이돌명으로 고유 키 생성
     */
    public static String generateGroupIdolKey(String groupName, String idolName) {
        return groupName.trim().toLowerCase() + "_" + idolName.trim().toLowerCase();
    }
    
    /**
     * 이미지 수 증가
     */
    public void incrementImageCount() {
        this.imageCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 이미지 수 감소
     */
    public void decrementImageCount() {
        if (this.imageCount > 0) {
            this.imageCount--;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
        this.groupIdolKey = generateGroupIdolKey(groupName, this.idolName);
    }
    
    public String getIdolName() {
        return idolName;
    }
    
    public void setIdolName(String idolName) {
        this.idolName = idolName;
        this.groupIdolKey = generateGroupIdolKey(this.groupName, idolName);
    }
    
    public String getGroupIdolKey() {
        return groupIdolKey;
    }
    
    public void setGroupIdolKey(String groupIdolKey) {
        this.groupIdolKey = groupIdolKey;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getImageCount() {
        return imageCount;
    }
    
    public void setImageCount(Integer imageCount) {
        this.imageCount = imageCount;
    }
    
    public List<IdolImage> getImages() {
        return images;
    }
    
    public void setImages(List<IdolImage> images) {
        this.images = images;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
