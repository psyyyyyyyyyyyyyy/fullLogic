package com.example.gpt_test.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Google Lens API 응답 DTO
 */
public class GoogleLensResponseDto {
    
    @JsonProperty("visual_matches")
    private List<VisualMatch> visualMatches;
    
    @JsonProperty("related_content")
    private List<RelatedContent> relatedContent;
    
    public static class VisualMatch {
        private int position;
        private String title;
        private String link;
        private String source;
        
        public int getPosition() {
            return position;
        }
        
        public void setPosition(int position) {
            this.position = position;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getLink() {
            return link;
        }
        
        public void setLink(String link) {
            this.link = link;
        }
        
        public String getSource() {
            return source;
        }
        
        public void setSource(String source) {
            this.source = source;
        }
    }
    
    public static class RelatedContent {
        private String query;
        private String link;
        
        public String getQuery() {
            return query;
        }
        
        public void setQuery(String query) {
            this.query = query;
        }
        
        public String getLink() {
            return link;
        }
        
        public void setLink(String link) {
            this.link = link;
        }
    }
    
    public List<VisualMatch> getVisualMatches() {
        return visualMatches;
    }
    
    public void setVisualMatches(List<VisualMatch> visualMatches) {
        this.visualMatches = visualMatches;
    }
    
    public List<RelatedContent> getRelatedContent() {
        return relatedContent;
    }
    
    public void setRelatedContent(List<RelatedContent> relatedContent) {
        this.relatedContent = relatedContent;
    }
}
