package com.example.gpt_test.service;

import com.example.gpt_test.dto.IdolUploadResponseDto;
import com.example.gpt_test.dto.PersonIdentificationResponseDto;
import com.example.gpt_test.entity.IdolImage;
import com.example.gpt_test.entity.User;
import com.example.gpt_test.entity.GroupIdol;
import com.example.gpt_test.repository.IdolImageRepository;
import com.example.gpt_test.util.ImageHashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 아이돌 이미지 관리 서비스
 */
@Service
public class IdolImageService {
    
    @Autowired
    private IdolImageRepository idolImageRepository;
    
    @Autowired
    private ImageHashUtil imageHashUtil;
    
    @Autowired
    private ImageAnalysisService imageAnalysisService;
    
    @Autowired
    private ProgressService progressService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private GroupIdolService groupIdolService;
    
    /**
     * 3장의 아이돌 이미지를 업로드하고 분석 (사용자별 개인 갤러리 + 그룹 DB 저장)
     */
    public IdolUploadResponseDto uploadAndAnalyzeIdolImages(String idolName, String groupName, MultipartFile[] images, String sessionId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 현재 인증된 사용자 조회
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                progressService.sendError(sessionId, "인증되지 않은 사용자입니다.");
                return new IdolUploadResponseDto(false, "인증되지 않은 사용자입니다.");
            }
            
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            // GroupIdol 조회 또는 생성
            GroupIdol groupIdol = groupIdolService.findOrCreateGroupIdol(groupName, idolName);
            
            progressService.sendProgress(sessionId, "validation", "👤 사용자: " + username + ", 그룹_아이돌: " + groupIdol.getGroupIdolKey());
            progressService.sendProgress(sessionId, "validation", "📋 업로드된 파일들을 검증하고 있습니다...");
            
            // 1. 기본 검증
            if (images == null || images.length != 3) {
                progressService.sendError(sessionId, "정확히 3장의 이미지를 업로드해야 합니다.");
                return new IdolUploadResponseDto(false, "정확히 3장의 이미지를 업로드해야 합니다.");
            }
            
            progressService.sendProgress(sessionId, "validation", "✅ 파일 개수 확인 완료 (3장)");
            
            // 2. 이미지 파일 유효성 검사
            progressService.sendProgress(sessionId, "validation", "🔍 이미지 파일 유효성을 검사하고 있습니다...");
            for (int i = 0; i < images.length; i++) {
                if (!imageHashUtil.isValidImageFile(images[i])) {
                    String errorMsg = (i + 1) + "번째 파일이 유효한 이미지 파일이 아닙니다.";
                    progressService.sendError(sessionId, errorMsg);
                    return new IdolUploadResponseDto(false, errorMsg);
                }
                progressService.sendProgress(sessionId, "validation", "✅ " + (i + 1) + "번째 이미지 파일 유효성 확인 완료");
            }
            
            // 3. pHash 생성 및 중복 검사 (개인 갤러리 내에서만)
            progressService.sendProgress(sessionId, "hash", "🔐 이미지 해시값을 생성하고 중복을 검사하고 있습니다...");
            List<String> newHashes = new ArrayList<>();
            List<String> userExistingHashes = idolImageRepository.findPHashesByUser(currentUser);
            
            progressService.sendProgress(sessionId, "hash", "📊 내 갤러리에서 " + userExistingHashes.size() + "개의 해시값을 가져왔습니다.");
            
            for (int i = 0; i < images.length; i++) {
                try {
                    progressService.sendProgress(sessionId, "hash", "🔐 " + (i + 1) + "번째 이미지의 해시값을 생성하고 있습니다...");
                    String pHash = imageHashUtil.generatePHash(images[i]);
                    progressService.sendProgress(sessionId, "hash", "✅ " + (i + 1) + "번째 이미지 해시값 생성 완료: " + pHash.substring(0, 8) + "...");
                    
                    // 내 갤러리와 중복 검사
                    progressService.sendProgress(sessionId, "hash", "🔍 " + (i + 1) + "번째 이미지의 내 갤러리 중복 검사 중...");
                    String duplicateHash = imageHashUtil.findDuplicateHash(pHash, userExistingHashes);
                    if (duplicateHash != null) {
                        String errorMsg = (i + 1) + "번째 이미지가 이미 내 갤러리에 존재하는 이미지와 유사합니다.";
                        progressService.sendError(sessionId, errorMsg);
                        return new IdolUploadResponseDto(false, errorMsg);
                    }
                    progressService.sendProgress(sessionId, "hash", "✅ " + (i + 1) + "번째 이미지 내 갤러리 중복 검사 통과");
                    
                    // 업로드하는 이미지들 간의 중복 검사
                    progressService.sendProgress(sessionId, "hash", "🔍 " + (i + 1) + "번째 이미지의 업로드 이미지 간 중복 검사 중...");
                    String duplicateInNew = imageHashUtil.findDuplicateHash(pHash, newHashes);
                    if (duplicateInNew != null) {
                        String errorMsg = (i + 1) + "번째 이미지가 다른 업로드 이미지와 중복됩니다.";
                        progressService.sendError(sessionId, errorMsg);
                        return new IdolUploadResponseDto(false, errorMsg);
                    }
                    progressService.sendProgress(sessionId, "hash", "✅ " + (i + 1) + "번째 이미지 중복 검사 모두 통과");
                    
                    newHashes.add(pHash);
                } catch (IOException e) {
                    String errorMsg = (i + 1) + "번째 이미지 처리 중 오류가 발생했습니다: " + e.getMessage();
                    progressService.sendError(sessionId, errorMsg);
                    return new IdolUploadResponseDto(false, errorMsg);
                }
            }
            
            progressService.sendProgress(sessionId, "hash", "🎉 모든 이미지의 중복 검사가 완료되었습니다!");
            
            // 4. 각 이미지에 대해 인물 분석 수행 (하나씩 순차적으로)
            progressService.sendProgress(sessionId, "analysis", "🔍 이제 각 이미지를 하나씩 순차적으로 분석합니다...");
            List<IdolUploadResponseDto.ImageAnalysisResult> results = new ArrayList<>();
            List<IdolImage> validatedImages = new ArrayList<>();
            
            for (int i = 0; i < images.length; i++) {
                try {
                    progressService.sendProgress(sessionId, "analysis", "📸 " + (i + 1) + "번째 이미지 (" + images[i].getOriginalFilename() + ") 분석을 시작합니다...");
                    progressService.sendProgress(sessionId, "analysis", "☁️ " + (i + 1) + "번째 이미지를 Cloudinary에 업로드하고 있습니다...");
                    
                    long imageStartTime = System.currentTimeMillis();
                    
                    // 기존 ImageAnalysisService를 사용하여 분석
                    PersonIdentificationResponseDto analysisResult = imageAnalysisService.identifyPersonFromFile(
                        images[i], groupName, idolName
                    );
                    
                    long imageProcessingTime = System.currentTimeMillis() - imageStartTime;
                    
                    progressService.sendProgress(sessionId, "analysis", "✅ " + (i + 1) + "번째 이미지 분석 완료! (" + imageProcessingTime + "ms)");
                    
                    // 결과 생성
                    IdolUploadResponseDto.ImageAnalysisResult result = new IdolUploadResponseDto.ImageAnalysisResult(
                        images[i].getOriginalFilename(),
                        analysisResult.getImageUrl(),
                        newHashes.get(i),
                        analysisResult.isMatch(),
                        analysisResult.getMatchReason(),
                        analysisResult.getIdentifiedPerson(),
                        analysisResult.getIdentifiedGroup(),
                        analysisResult.getGptAnalysis(),
                        imageProcessingTime
                    );
                    
                    results.add(result);
                    
                    // 분석 결과 요약 전송
                    String resultSummary = String.format("식별된 인물: %s (%s), 일치: %s", 
                        analysisResult.getIdentifiedPerson(), 
                        analysisResult.getIdentifiedGroup(),
                        analysisResult.isMatch() ? "✅" : "❌");
                    progressService.sendProgress(sessionId, "analysis", resultSummary);
                    
                    // 모든 이미지를 개인 갤러리에 저장 (일치 여부와 관계없이)
                    progressService.sendProgress(sessionId, "save", "💾 " + (i + 1) + "번째 이미지를 개인 갤러리에 저장합니다...");
                    
                    IdolImage idolImage = new IdolImage(
                        idolName,
                        groupName,
                        result.getImageUrl(),
                        result.getpHash(),
                        result.getFileName(),
                        images[i].getSize(),
                        images[i].getContentType(),
                        currentUser,
                        groupIdol
                    );
                    
                    idolImage.setGptAnalysis(result.getGptAnalysis());
                    
                    // GPT 분석 결과가 일치하면 그룹 DB에도 추가
                    if (analysisResult.isMatch()) {
                        idolImage.addToGroupDatabase(); // isInGroupDatabase = true, isVerified = true
                        progressService.sendProgress(sessionId, "save", "✅ " + (i + 1) + "번째 이미지가 일치하므로 그룹 DB에도 추가됩니다!");
                        
                        // 그룹_아이돌의 이미지 수 증가
                        groupIdolService.incrementImageCount(groupIdol);
                    } else {
                        idolImage.setIsVerified(false);
                        progressService.sendProgress(sessionId, "save", "⚠️ " + (i + 1) + "번째 이미지는 개인 갤러리에만 저장됩니다.");
                    }
                    
                    IdolImage savedImage = idolImageRepository.save(idolImage);
                    validatedImages.add(savedImage);
                    
                    progressService.sendProgress(sessionId, "save", "✅ " + (i + 1) + "번째 이미지 저장 완료 (ID: " + savedImage.getId() + ")");
                    
                } catch (Exception e) {
                    String errorMsg = (i + 1) + "번째 이미지 분석 중 오류가 발생했습니다: " + e.getMessage();
                    progressService.sendError(sessionId, errorMsg);
                    return new IdolUploadResponseDto(false, errorMsg);
                }
            }
            
            progressService.sendProgress(sessionId, "analysis", "🎉 모든 이미지 분석이 완료되었습니다!");
            
            // 5. 분석 결과 요약
            long matchedCount = results.stream().mapToLong(r -> r.isMatch() ? 1 : 0).sum();
            progressService.sendProgress(sessionId, "validation", "📊 분석 결과: " + matchedCount + "개 일치, " + (results.size() - matchedCount) + "개 불일치");
            
            // 6. DB에 저장된 해당 아이돌의 모든 이미지 조회
            progressService.sendProgress(sessionId, "database", "📂 DB에서 " + idolName + " (" + groupName + ")의 기존 이미지들을 조회하고 있습니다...");
            List<IdolImage> existingImages = idolImageRepository.findByIdolNameAndGroupName(idolName, groupName);
            progressService.sendProgress(sessionId, "database", "📂 DB에서 " + existingImages.size() + "개의 기존 이미지를 찾았습니다.");
            
            long totalProcessingTime = System.currentTimeMillis() - startTime;
            
            // 결과 메시지 생성
            String resultMessage;
            if (matchedCount == results.size()) {
                resultMessage = "모든 이미지가 성공적으로 검증되고 DB에 저장되었습니다.";
                progressService.sendProgress(sessionId, "complete", "🎉 모든 과정이 성공적으로 완료되었습니다! (총 " + totalProcessingTime + "ms)");
            } else if (matchedCount > 0) {
                resultMessage = matchedCount + "개의 이미지가 검증되어 DB에 저장되었습니다. " + (results.size() - matchedCount) + "개는 일치하지 않았습니다.";
                progressService.sendProgress(sessionId, "complete", "⚠️ 일부 이미지만 검증되었습니다. (총 " + totalProcessingTime + "ms)");
            } else {
                resultMessage = "일치하는 이미지가 없어서 DB에 저장된 이미지가 없습니다.";
                progressService.sendProgress(sessionId, "complete", "❌ 일치하는 이미지가 없습니다. (총 " + totalProcessingTime + "ms)");
            }
            
            // 응답 생성 (기존 이미지 정보 포함)
            IdolUploadResponseDto response = new IdolUploadResponseDto(
                matchedCount > 0, 
                resultMessage, 
                results, 
                totalProcessingTime
            );
            
            // 기존 이미지 정보를 응답에 추가
            response.setExistingImages(existingImages);
            
            progressService.sendComplete(sessionId, response);
            return response;
            
        } catch (Exception e) {
            return new IdolUploadResponseDto(false, "처리 중 예상치 못한 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 특정 아이돌의 모든 이미지 조회
     */
    public List<IdolImage> getIdolImages(String idolName, String groupName) {
        return idolImageRepository.findByIdolNameAndGroupName(idolName, groupName);
    }
    
    /**
     * 검증된 모든 이미지 조회
     */
    public List<IdolImage> getAllVerifiedImages() {
        return idolImageRepository.findByIsVerifiedTrue();
    }
    
    /**
     * 이미지 삭제 (사용자 권한 확인)
     */
    public boolean deleteImage(Long imageId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }
            
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username).orElse(null);
            if (currentUser == null) {
                return false;
            }
            
            // 이미지 조회 및 소유자 확인
            IdolImage image = idolImageRepository.findById(imageId).orElse(null);
            if (image == null || !image.getUser().getId().equals(currentUser.getId())) {
                return false;
            }
            
            // 그룹 DB에서 제거되는 경우 이미지 수 감소
            if (image.getIsInGroupDatabase()) {
                groupIdolService.decrementImageCount(image.getGroupIdol());
            }
            
            idolImageRepository.deleteById(imageId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // === 새로운 사용자별/그룹별 조회 메서드 ===
    
    /**
     * 현재 사용자의 개인 갤러리 이미지 조회
     */
    public List<IdolImage> getMyPersonalGallery() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ArrayList<>();
        }
        
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        return idolImageRepository.findByUserAndIsInPersonalGalleryTrue(currentUser);
    }
    
    /**
     * 현재 사용자의 특정 아이돌 이미지 조회
     */
    public List<IdolImage> getMyIdolImages(String idolName, String groupName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ArrayList<>();
        }
        
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        return idolImageRepository.findByUserAndIdolNameAndGroupNameAndIsInPersonalGalleryTrue(
            currentUser, idolName, groupName);
    }
    
    /**
     * 그룹 DB의 공유 이미지 조회
     */
    public List<IdolImage> getGroupSharedImages(String groupName, String idolName) {
        String groupIdolKey = GroupIdol.generateGroupIdolKey(groupName, idolName);
        return idolImageRepository.findGroupSharedImages(groupIdolKey);
    }
    
    /**
     * 모든 그룹 DB 이미지 조회
     */
    public List<IdolImage> getAllGroupSharedImages() {
        return idolImageRepository.findByIsInGroupDatabaseTrueAndIsVerifiedTrue();
    }
    
    /**
     * 현재 사용자가 업로드한 모든 이미지 조회
     */
    public List<IdolImage> getMyAllImages() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ArrayList<>();
        }
        
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        return idolImageRepository.findByUser(currentUser);
    }
    
    /**
     * 특정 아이돌의 모든 사용자 이미지 조회 (업로더 정보 포함)
     */
    public List<IdolImage> getIdolGalleryWithUploaders(String idolName, String groupName) {
        return idolImageRepository.findByIdolNameAndGroupNameWithUser(idolName, groupName);
    }
}
