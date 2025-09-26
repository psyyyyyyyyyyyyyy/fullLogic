package com.example.gpt_test.controller;

import com.example.gpt_test.dto.IdolUploadResponseDto;
import com.example.gpt_test.entity.IdolImage;
import com.example.gpt_test.service.IdolImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.example.gpt_test.service.ProgressService;

import java.util.List;
import java.util.UUID;

/**
 * 아이돌 이미지 업로드 및 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/idol")
@Tag(name = "Idol Image Management", description = "아이돌 이미지 업로드 및 관리 API")
public class IdolImageController {
    
    @Autowired
    private IdolImageService idolImageService;
    
    @Autowired
    private ProgressService progressService;
    
    /**
     * 아이돌 이미지 3장 업로드 및 분석
     */
    /**
     * 진행 상황을 실시간으로 받기 위한 SSE 연결
     */
    @GetMapping("/progress/{sessionId}")
    @Operation(summary = "진행 상황 스트림", description = "업로드 진행 상황을 실시간으로 받습니다.")
    public SseEmitter getProgress(@PathVariable String sessionId) {
        return progressService.addEmitter(sessionId);
    }
    
    @PostMapping("/upload")
    @Operation(summary = "아이돌 이미지 업로드", description = "아이돌 이름과 그룹명을 입력하고 3장의 이미지를 업로드하여 분석합니다.")
    public ResponseEntity<IdolUploadResponseDto> uploadIdolImages(
            @Parameter(description = "아이돌 이름", required = true)
            @RequestParam @NotBlank String idolName,
            
            @Parameter(description = "그룹명", required = true)
            @RequestParam @NotBlank String groupName,
            
            @Parameter(description = "업로드할 3장의 이미지 파일", required = true)
            @RequestParam("images") @NotNull MultipartFile[] images,
            
            @Parameter(description = "세션 ID (진행 상황 추적용)", required = false)
            @RequestParam(required = false) String sessionId) {
        
        // 세션 ID가 없으면 생성
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        
        IdolUploadResponseDto response = idolImageService.uploadAndAnalyzeIdolImages(idolName, groupName, images, sessionId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 특정 아이돌의 이미지 목록 조회
     */
    @GetMapping("/images")
    @Operation(summary = "아이돌 이미지 조회", description = "특정 아이돌의 모든 이미지를 조회합니다.")
    public ResponseEntity<List<IdolImage>> getIdolImages(
            @Parameter(description = "아이돌 이름", required = true)
            @RequestParam @NotBlank String idolName,
            
            @Parameter(description = "그룹명", required = true)
            @RequestParam @NotBlank String groupName) {
        
        List<IdolImage> images = idolImageService.getIdolImages(idolName, groupName);
        return ResponseEntity.ok(images);
    }
    
    /**
     * 특정 아이돌의 이미지 목록을 JSON으로 조회 (프론트엔드용)
     */
    @GetMapping("/images/json")
    @Operation(summary = "아이돌 이미지 JSON 조회", description = "특정 아이돌의 모든 이미지를 JSON 형태로 조회합니다.")
    public ResponseEntity<List<IdolImage>> getIdolImagesJson(
            @Parameter(description = "아이돌 이름", required = true)
            @RequestParam @NotBlank String idolName,
            
            @Parameter(description = "그룹명", required = true)
            @RequestParam @NotBlank String groupName) {
        
        try {
            List<IdolImage> images = idolImageService.getIdolImages(idolName, groupName);
            return ResponseEntity.ok()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(images);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 검증된 모든 이미지 조회
     */
    @GetMapping("/images/verified")
    @Operation(summary = "검증된 이미지 조회", description = "검증된 모든 아이돌 이미지를 조회합니다.")
    public ResponseEntity<List<IdolImage>> getAllVerifiedImages() {
        List<IdolImage> images = idolImageService.getAllVerifiedImages();
        return ResponseEntity.ok(images);
    }
    
    /**
     * 이미지 삭제
     */
    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "이미지 삭제", description = "특정 이미지를 삭제합니다.")
    public ResponseEntity<String> deleteImage(
            @Parameter(description = "삭제할 이미지 ID", required = true)
            @PathVariable Long imageId) {
        
        boolean deleted = idolImageService.deleteImage(imageId);
        
        if (deleted) {
            return ResponseEntity.ok("이미지가 성공적으로 삭제되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("이미지 삭제에 실패했습니다.");
        }
    }
    
    /**
     * 서비스 상태 확인
     */
    @GetMapping("/health")
    @Operation(summary = "서비스 상태 확인", description = "아이돌 이미지 서비스의 상태를 확인합니다.")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("아이돌 이미지 서비스가 정상적으로 작동 중입니다.");
    }
    
    /**
     * 테스트용 더미 데이터 조회
     */
    @GetMapping("/test")
    @Operation(summary = "테스트 데이터", description = "테스트용 더미 데이터를 반환합니다.")
    public ResponseEntity<String> testData() {
        return ResponseEntity.ok("{\"message\": \"테스트 성공\", \"timestamp\": \"" + System.currentTimeMillis() + "\"}");
    }
}
