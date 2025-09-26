package com.example.gpt_test.dto;

/**
 * 이미지 분석 요청 DTO
 */
public class GoogleLensRequestDto {
    
    private String imageUrl;
    private String favoriteGroup;  // 사용자가 좋아하는 그룹명
    private String favoriteName;   // 사용자가 좋아하는 인물명
    
    public GoogleLensRequestDto() {}
    
    public GoogleLensRequestDto(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public GoogleLensRequestDto(String imageUrl, String favoriteGroup, String favoriteName) {
        this.imageUrl = imageUrl;
        this.favoriteGroup = favoriteGroup;
        this.favoriteName = favoriteName;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getFavoriteGroup() {
        return favoriteGroup;
    }
    
    public void setFavoriteGroup(String favoriteGroup) {
        this.favoriteGroup = favoriteGroup;
    }
    
    public String getFavoriteName() {
        return favoriteName;
    }
    
    public void setFavoriteName(String favoriteName) {
        this.favoriteName = favoriteName;
    }
}
