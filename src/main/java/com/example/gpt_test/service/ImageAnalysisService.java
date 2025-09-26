package com.example.gpt_test.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.gpt_test.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 이미지 분석 서비스 (Google Lens API 연동)
 */
@Service
public class ImageAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisService.class);
    
    @Value("${serp.api.key}")
    private String serpApiKey;
    
    @Value("${serp.api.url}")
    private String serpApiUrl;
    
    private final RestTemplate restTemplate;
    private final ChatService chatService;
    private final Cloudinary cloudinary;
    
    public ImageAnalysisService(RestTemplate restTemplate, ChatService chatService, Cloudinary cloudinary) {
        this.restTemplate = restTemplate;
        this.chatService = chatService;
        this.cloudinary = cloudinary;
    }
    
    /**
     * 파일을 업로드하여 Cloudinary에 저장하고 인물을 식별하는 메인 메서드
     */
    public PersonIdentificationResponseDto identifyPersonFromFile(MultipartFile file, String favoriteGroup, String favoriteName) {
        long startTime = System.currentTimeMillis();
        String publicId = null; // 업로드된 이미지의 public_id 저장
        
        try {
            logger.info("파일 업로드 및 분석 시작: {}", file.getOriginalFilename());
            
            // 1. Cloudinary에 파일 업로드
            CloudinaryUploadResult uploadResult = uploadToCloudinaryWithId(file);
            String imageUrl = uploadResult.getSecureUrl();
            publicId = uploadResult.getPublicId();
            
            // 2. 업로드된 URL로 인물 식별
            GoogleLensRequestDto request = new GoogleLensRequestDto(imageUrl, favoriteGroup, favoriteName);
            PersonIdentificationResponseDto result = identifyPersonFromImage(request);
            
            // 3. 분석 완료 후 - 일치하는 경우에만 이미지 보관, 불일치하면 삭제
            if (result.isMatch()) {
                logger.info("일치하는 이미지이므로 Cloudinary에 보관합니다: {}", publicId);
                // 이미지를 삭제하지 않고 보관
            } else {
                logger.info("불일치하는 이미지이므로 Cloudinary에서 삭제합니다: {}", publicId);
                deleteFromCloudinary(publicId);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("파일 업로드 및 분석 중 오류 발생: {}", e.getMessage(), e);
            
            // 오류 발생 시에도 업로드된 이미지 삭제 시도
            if (publicId != null) {
                deleteFromCloudinary(publicId);
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return new PersonIdentificationResponseDto(
                "",
                new ArrayList<>(),
                new ArrayList<>(),
                "파일 업로드 중 오류가 발생했습니다: " + e.getMessage(),
                "unknown",
                "unknown",
                false,
                "파일 업로드 오류로 인해 판별 불가",
                processingTime
            );
        }
    }
    
    /**
     * Cloudinary 업로드 결과를 담는 내부 클래스
     */
    private static class CloudinaryUploadResult {
        private final String secureUrl;
        private final String publicId;
        
        public CloudinaryUploadResult(String secureUrl, String publicId) {
            this.secureUrl = secureUrl;
            this.publicId = publicId;
        }
        
        public String getSecureUrl() { return secureUrl; }
        public String getPublicId() { return publicId; }
    }
    
    /**
     * Cloudinary에 파일 업로드 (public_id 포함 반환)
     */
    private CloudinaryUploadResult uploadToCloudinaryWithId(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        
        // 파일 크기 제한 (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
        
        // 이미지 파일인지 확인
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        
        try {
            logger.info("Cloudinary 업로드 시작: {} ({}bytes)", file.getOriginalFilename(), file.getSize());
            
            // Cloudinary 업로드 옵션 설정
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                "resource_type", "image",
                "folder", "gpt_test_images", // 폴더명 설정
                "use_filename", true,
                "unique_filename", true,
                "overwrite", false,
                "format", "jpg" // 자동으로 JPG로 변환
            );
            
            // 파일 업로드 실행
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            
            logger.info("Cloudinary 업로드 성공: public_id={}, url={}", publicId, secureUrl);
            
            return new CloudinaryUploadResult(secureUrl, publicId);
            
        } catch (IOException e) {
            logger.error("Cloudinary 업로드 실패: {}", e.getMessage(), e);
            throw new IOException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cloudinary에서 이미지 삭제
     */
    private void deleteFromCloudinary(String publicId) {
        if (publicId == null || publicId.trim().isEmpty()) {
            logger.warn("삭제할 public_id가 없습니다.");
            return;
        }
        
        try {
            logger.info("Cloudinary에서 이미지 삭제 시작: {}", publicId);
            
            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String result = (String) deleteResult.get("result");
            
            if ("ok".equals(result)) {
                logger.info("Cloudinary 이미지 삭제 성공: {}", publicId);
            } else {
                logger.warn("Cloudinary 이미지 삭제 결과: {}, public_id: {}", result, publicId);
            }
            
        } catch (Exception e) {
            logger.error("Cloudinary 이미지 삭제 실패: public_id={}, 오류={}", publicId, e.getMessage());
            // 삭제 실패해도 전체 프로세스는 계속 진행
        }
    }
    
    /**
     * Cloudinary에 파일 업로드 (기존 호환성을 위한 메서드)
     */
    private String uploadToCloudinary(MultipartFile file) throws IOException {
        CloudinaryUploadResult result = uploadToCloudinaryWithId(file);
        return result.getSecureUrl();
    }
    
    /**
     * 이미지를 분석하고 인물을 식별하는 메인 메서드
     */
    public PersonIdentificationResponseDto identifyPersonFromImage(GoogleLensRequestDto request) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("이미지 분석 시작: {}", request.getImageUrl());
            
            // 1. Google Lens API를 통해 이미지 분석
            GoogleLensResponseDto googleLensResponse = callGoogleLensApi(request.getImageUrl());
            
            // 2. title과 query 값들 추출
            List<String> titles = extractTitles(googleLensResponse);
            List<String> queries = extractQueries(googleLensResponse);
            
            logger.info("추출된 titles: {}, queries: {}", titles, queries);
            
            // 구글 렌즈 데이터가 없는 경우 즉시 불일치 반환
            if ((titles == null || titles.isEmpty()) && (queries == null || queries.isEmpty())) {
                logger.info("구글 렌즈에서 추출된 데이터가 없어 불일치 반환");
                long processingTime = System.currentTimeMillis() - startTime;
                
                return new PersonIdentificationResponseDto(
                    request.getImageUrl(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    "{\"name\": \"unknown\", \"group_name\": \"unknown\", \"is_match\": false, \"match_reason\": \"no data extracted from google lens\"}",
                    "unknown",
                    "unknown",
                    false,
                    "구글 렌즈에서 분석 데이터를 추출하지 못했습니다",
                    processingTime
                );
            }
            
            // 3. GPT에게 인물 식별 요청
            String gptPrompt = createIdentificationPrompt(titles, queries, request.getFavoriteGroup(), request.getFavoriteName());
            ChatRequestDto chatRequest = new ChatRequestDto(gptPrompt);
            ChatResponseDto gptResponse = chatService.getChatResponse(chatRequest);
            
            // 4. 결과 반환
            long processingTime = System.currentTimeMillis() - startTime;
            
            // JSON 응답에서 데이터 추출
            String jsonResponse = gptResponse.getAnswer();
            logger.info("GPT 원본 응답: {}", jsonResponse);
            
            String identifiedPerson = extractPersonName(jsonResponse);
            String identifiedGroup = extractGroupName(jsonResponse);
            boolean isMatch = extractMatchResult(jsonResponse);
            String matchReason = extractMatchReason(jsonResponse);
            
            logger.info("파싱 결과 - 인물: {}, 그룹: {}, 일치: {}, 이유: {}", 
                identifiedPerson, identifiedGroup, isMatch, matchReason);
            
            return new PersonIdentificationResponseDto(
                request.getImageUrl(),
                titles,
                queries,
                jsonResponse,
                identifiedPerson,
                identifiedGroup,
                isMatch,
                matchReason,
                processingTime
            );
            
        } catch (Exception e) {
            logger.error("이미지 분석 중 오류 발생: {}", e.getMessage(), e);
            long processingTime = System.currentTimeMillis() - startTime;
            
            return new PersonIdentificationResponseDto(
                request.getImageUrl(),
                new ArrayList<>(),
                new ArrayList<>(),
                "분석 중 오류가 발생했습니다: " + e.getMessage(),
                "unknown",
                "unknown",
                false,
                "분석 오류로 인해 판별 불가",
                processingTime
            );
        }
    }
    
    /**
     * Google Lens API 호출
     */
    private GoogleLensResponseDto callGoogleLensApi(String imageUrl) {
        try {
            String url = UriComponentsBuilder.fromUriString(serpApiUrl)
                    .queryParam("engine", "google_lens")
                    .queryParam("url", imageUrl)
                    .queryParam("api_key", serpApiKey)
                    .toUriString();
            
            logger.info("Google Lens API 호출: {}", url);
            
            GoogleLensResponseDto response = restTemplate.getForObject(url, GoogleLensResponseDto.class);
            
            if (response == null) {
                throw new RuntimeException("Google Lens API에서 응답을 받지 못했습니다.");
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Google Lens API 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Google Lens API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * visual_matches에서 title 값들 추출
     */
    private List<String> extractTitles(GoogleLensResponseDto response) {
        if (response.getVisualMatches() == null) {
            return new ArrayList<>();
        }
        
        return response.getVisualMatches().stream()
                .filter(match -> match.getTitle() != null && !match.getTitle().trim().isEmpty())
                .map(GoogleLensResponseDto.VisualMatch::getTitle)
                .limit(20) // 최대 20개까지만
                .collect(Collectors.toList());
    }
    
    /**
     * related_content에서 query 값들 추출
     */
    private List<String> extractQueries(GoogleLensResponseDto response) {
        if (response.getRelatedContent() == null) {
            return new ArrayList<>();
        }
        
        return response.getRelatedContent().stream()
                .filter(content -> content.getQuery() != null && !content.getQuery().trim().isEmpty())
                .map(GoogleLensResponseDto.RelatedContent::getQuery)
                .limit(5) // 최대 5개까지만
                .collect(Collectors.toList());
    }
    
    /**
     * GPT에게 보낼 인물 식별 프롬프트 생성
     */
    private String createIdentificationPrompt(List<String> titles, List<String> queries, String favoriteGroup, String favoriteName) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following Google Lens image search results to identify the person in the photo. ");
        prompt.append("Return ONLY a JSON response with the format: ");
        prompt.append("{\"name\": \"person_name\", \"group_name\": \"group_or_solo\", \"is_match\": true/false, \"match_reason\": \"reason\"}\n\n");
        
        if (!titles.isEmpty()) {
            prompt.append("Search result titles:\n");
            for (int i = 0; i < titles.size(); i++) {
                prompt.append(String.format("- %s\n", titles.get(i)));
            }
            prompt.append("\n");
        }
        
        if (!queries.isEmpty()) {
            prompt.append("Related queries:\n");
            for (int i = 0; i < queries.size(); i++) {
                prompt.append(String.format("- %s\n", queries.get(i)));
            }
            prompt.append("\n");
        }
        
        // 사용자 선호 정보 추가
        if (favoriteGroup != null || favoriteName != null) {
            prompt.append("User's favorite:\n");
            if (favoriteName != null && !favoriteName.trim().isEmpty()) {
                prompt.append(String.format("- Favorite person: %s\n", favoriteName));
            }
            if (favoriteGroup != null && !favoriteGroup.trim().isEmpty()) {
                prompt.append(String.format("- Favorite group: %s\n", favoriteGroup));
            }
            prompt.append("\n");
        }
        
        prompt.append("Rules:\n");
        prompt.append("- Use only lowercase English letters and numbers\n");
        prompt.append("- For name: use the person's stage name or commonly known name\n");
        prompt.append("- For group_name: use the group/band name if applicable, or \"solo\" if solo artist\n");
        prompt.append("- For is_match: compare identified person/group with user's favorite and return true if they match, false otherwise\n");
        prompt.append("- For match_reason: explain why they match or don't match (e.g., \"same person\", \"same group\", \"different person\", \"different group\")\n");
        prompt.append("- If you recognize the person, fill in any missing information\n");
        prompt.append("- If uncertain, use \"unknown\" for unknown fields\n");
        prompt.append("- Return ONLY the JSON, no additional text\n");
        
        return prompt.toString();
    }
    
    /**
     * GPT 응답에서 인물 이름 추출 (JSON 파싱)
     */
    private String extractPersonName(String gptResponse) {
        return extractJsonValue(gptResponse, "name", "unknown");
    }
    
    /**
     * GPT 응답에서 그룹명 추출 (JSON 파싱)
     */
    private String extractGroupName(String gptResponse) {
        return extractJsonValue(gptResponse, "group_name", "unknown");
    }
    
    /**
     * GPT 응답에서 일치 여부 추출 (JSON 파싱)
     */
    private boolean extractMatchResult(String gptResponse) {
        String value = extractJsonValue(gptResponse, "is_match", "false");
        return "true".equalsIgnoreCase(value);
    }
    
    /**
     * GPT 응답에서 일치 이유 추출 (JSON 파싱)
     */
    private String extractMatchReason(String gptResponse) {
        return extractJsonValue(gptResponse, "match_reason", "분석 결과를 확인할 수 없습니다");
    }
    
    /**
     * JSON에서 특정 키의 값을 추출하는 공통 메서드
     */
    private String extractJsonValue(String gptResponse, String key, String defaultValue) {
        if (gptResponse == null || gptResponse.trim().isEmpty()) {
            logger.warn("GPT 응답이 비어있음");
            return defaultValue;
        }
        
        try {
            // JSON에서 해당 키 필드 추출
            String searchKey = "\"" + key + "\"";
            logger.debug("키 '{}' 검색 중, 응답 길이: {}", key, gptResponse.length());
            
            if (gptResponse.contains(searchKey)) {
                int keyStart = gptResponse.indexOf(searchKey);
                int colonPos = gptResponse.indexOf(":", keyStart);
                
                if (colonPos == -1) {
                    logger.warn("키 '{}' 다음에 콜론을 찾을 수 없음", key);
                    return defaultValue;
                }
                
                // boolean 값인 경우
                if (key.equals("is_match")) {
                    String afterColon = gptResponse.substring(colonPos + 1).trim();
                    // 쉼표나 괄호 제거
                    afterColon = afterColon.replaceAll("[,}\\s].*", "");
                    
                    if (afterColon.startsWith("true")) {
                        return "true";
                    } else if (afterColon.startsWith("false")) {
                        return "false";
                    }
                    logger.warn("is_match 값을 파싱할 수 없음: {}", afterColon);
                } else {
                    // 문자열 값인 경우
                    String afterColon = gptResponse.substring(colonPos + 1).trim();
                    
                    // 따옴표로 감싸진 문자열 찾기
                    int valueStart = afterColon.indexOf("\"");
                    if (valueStart != -1) {
                        valueStart += 1; // 따옴표 다음부터
                        int valueEnd = afterColon.indexOf("\"", valueStart);
                        
                        if (valueEnd > valueStart) {
                            String result = afterColon.substring(valueStart, valueEnd);
                            logger.debug("키 '{}' 값 추출 성공: {}", key, result);
                            return result;
                        }
                    }
                    
                    // 따옴표가 없는 경우 (쉼표나 괄호까지)
                    String value = afterColon.replaceAll("^[\\s\"]*", "").replaceAll("[,}\\s\"]*$", "");
                    if (!value.isEmpty() && !value.equals("null")) {
                        logger.debug("키 '{}' 값 추출 (따옴표 없음): {}", key, value);
                        return value;
                    }
                    
                    logger.warn("키 '{}' 값을 파싱할 수 없음, afterColon: {}", key, afterColon);
                }
            } else {
                logger.warn("키 '{}'를 응답에서 찾을 수 없음", key);
            }
        } catch (Exception e) {
            logger.warn("JSON 파싱 중 오류 (key: {}): {}", key, e.getMessage());
        }
        
        return defaultValue;
    }
}
