package com.example.gpt_test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * 아이돌 이미지 업로드 요청 DTO
 */
public class IdolUploadRequestDto {
    
    @NotBlank(message = "아이돌 이름은 필수입니다")
    private String idolName;
    
    @NotBlank(message = "그룹명은 필수입니다")
    private String groupName;
    
    @NotNull(message = "이미지 파일들은 필수입니다")
    @Size(min = 3, max = 3, message = "정확히 3장의 이미지를 업로드해야 합니다")
    private MultipartFile[] images;
    
    public IdolUploadRequestDto() {}
    
    public IdolUploadRequestDto(String idolName, String groupName, MultipartFile[] images) {
        this.idolName = idolName;
        this.groupName = groupName;
        this.images = images;
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
    
    public MultipartFile[] getImages() {
        return images;
    }
    
    public void setImages(MultipartFile[] images) {
        this.images = images;
    }
}
