package com.example.gpt_test.controller;

import com.example.gpt_test.dto.GoogleLensRequestDto;
import com.example.gpt_test.dto.PersonIdentificationResponseDto;
import com.example.gpt_test.service.ImageAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 분석 및 인물 식별 컨트롤러
 */
@RestController
@RequestMapping("/api/image")
@Tag(name = "Image Analysis", description = "이미지 분석 및 인물 식별 API")
public class ImageAnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisController.class);
    
    private final ImageAnalysisService imageAnalysisService;
    
    public ImageAnalysisController(ImageAnalysisService imageAnalysisService) {
        this.imageAnalysisService = imageAnalysisService;
    }
    
    /**
     * 이미지 URL을 받아서 인물을 식별하는 API
     * 
     * @param request 이미지 URL이 포함된 요청 객체
     * @return 인물 식별 결과
     */
    @PostMapping("/identify-person")
    @Operation(summary = "이미지에서 인물 식별", description = "이미지 URL을 받아서 Google Lens로 분석하고 GPT로 인물을 식별합니다.")
    public ResponseEntity<PersonIdentificationResponseDto> identifyPerson(@RequestBody GoogleLensRequestDto request) {
        
        if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
            logger.warn("빈 이미지 URL로 요청됨");
            return ResponseEntity.badRequest().build();
        }
        
        // URL 길이 제한 (로깅용)
        String shortUrl = request.getImageUrl().length() > 100 
            ? request.getImageUrl().substring(0, 100) + "..."
            : request.getImageUrl();
        
        logger.info("인물 식별 요청 - 이미지 URL: {}", shortUrl);
        
        try {
            PersonIdentificationResponseDto response = imageAnalysisService.identifyPersonFromImage(request);
            
            logger.info("인물 식별 완료 - URL: {}, 식별된 인물: {}, 처리시간: {}ms", 
                       shortUrl, response.getIdentifiedPerson(), response.getProcessingTime());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("인물 식별 중 오류 발생 - URL: {}, 오류: {}", shortUrl, e.getMessage(), e);
            
            // 오류가 발생해도 기본 응답 구조는 유지
            PersonIdentificationResponseDto errorResponse = new PersonIdentificationResponseDto(
                request.getImageUrl(),
                null,
                null,
                "처리 중 오류가 발생했습니다: " + e.getMessage(),
                "unknown",
                "unknown",
                false,
                "분석 오류로 인해 판별 불가",
                0
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 서비스 상태 확인 API
     * 
     * @return 서비스 상태 정보
     */
    @GetMapping("/health")
    @Operation(summary = "이미지 분석 서비스 상태 확인")
    public ResponseEntity<String> checkHealth() {
        logger.info("이미지 분석 서비스 상태 확인 요청");
        return ResponseEntity.ok("{\"status\": \"OK\", \"service\": \"Image Analysis Service\", \"timestamp\": " + System.currentTimeMillis() + "}");
    }
    
    /**
     * 간단한 테스트용 GET API (이미지 URL을 파라미터로 받음)
     * 
     * @param imageUrl 분석할 이미지 URL
     * @return 인물 식별 결과
     */
    @GetMapping("/identify-person")
    @Operation(summary = "이미지에서 인물 식별 (GET)", description = "테스트용 GET API - 이미지 URL을 파라미터로 받아서 인물을 식별합니다.")
    public ResponseEntity<PersonIdentificationResponseDto> identifyPersonByUrl(@RequestParam String imageUrl) {
        
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            logger.warn("빈 이미지 URL로 GET 요청됨");
            return ResponseEntity.badRequest().build();
        }
        
        GoogleLensRequestDto request = new GoogleLensRequestDto(imageUrl);
        return identifyPerson(request);
    }
    
    /**
     * 파일 업로드를 통한 인물 식별 API
     * 
     * @param file 업로드할 이미지 파일
     * @param favoriteGroup 사용자 선호 그룹 (선택사항)
     * @param favoriteName 사용자 선호 인물 (선택사항)
     * @return 인물 식별 결과
     */
    @PostMapping("/upload-and-identify")
    @Operation(summary = "파일 업로드 후 인물 식별", description = "이미지 파일을 Cloudinary에 업로드하고 Google Lens로 분석하여 인물을 식별합니다.")
    public ResponseEntity<PersonIdentificationResponseDto> uploadAndIdentifyPerson(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "favoriteGroup", required = false) String favoriteGroup,
            @RequestParam(value = "favoriteName", required = false) String favoriteName) {
        
        if (file == null || file.isEmpty()) {
            logger.warn("빈 파일로 업로드 요청됨");
            return ResponseEntity.badRequest().build();
        }
        
        // 파일명을 로깅용으로 짧게 표시
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        String shortFileName = fileName.length() > 50 
            ? fileName.substring(0, 50) + "..."
            : fileName;
        
        logger.info("파일 업로드 및 인물 식별 요청 - 파일: {}, 크기: {}bytes", shortFileName, file.getSize());
        
        try {
            PersonIdentificationResponseDto response = imageAnalysisService.identifyPersonFromFile(file, favoriteGroup, favoriteName);
            
            logger.info("파일 업로드 및 인물 식별 완료 - 파일: {}, 식별된 인물: {}, 처리시간: {}ms", 
                       shortFileName, response.getIdentifiedPerson(), response.getProcessingTime());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("파일 업로드 및 인물 식별 중 오류 발생 - 파일: {}, 오류: {}", shortFileName, e.getMessage(), e);
            
            // 오류가 발생해도 기본 응답 구조는 유지
            PersonIdentificationResponseDto errorResponse = new PersonIdentificationResponseDto(
                "",
                null,
                null,
                "파일 처리 중 오류가 발생했습니다: " + e.getMessage(),
                "unknown",
                "unknown",
                false,
                "파일 처리 오류로 인해 판별 불가",
                0
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
