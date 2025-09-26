package com.example.gpt_test.service;

import com.example.gpt_test.dto.IdolUploadResponseDto;
import com.example.gpt_test.dto.PersonIdentificationResponseDto;
import com.example.gpt_test.entity.IdolImage;
import com.example.gpt_test.repository.IdolImageRepository;
import com.example.gpt_test.util.ImageHashUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    /**
     * 3장의 아이돌 이미지를 업로드하고 분석
     */
    public IdolUploadResponseDto uploadAndAnalyzeIdolImages(String idolName, String groupName, MultipartFile[] images, String sessionId) {
        long startTime = System.currentTimeMillis();
        
        try {
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
            
            // 3. pHash 생성 및 중복 검사
            progressService.sendProgress(sessionId, "hash", "🔐 이미지 해시값을 생성하고 중복을 검사하고 있습니다...");
            List<String> newHashes = new ArrayList<>();
            List<String> existingHashes = idolImageRepository.findAllPHashes();
            
            progressService.sendProgress(sessionId, "hash", "📊 기존 DB에서 " + existingHashes.size() + "개의 해시값을 가져왔습니다.");
            
            for (int i = 0; i < images.length; i++) {
                try {
                    progressService.sendProgress(sessionId, "hash", "🔐 " + (i + 1) + "번째 이미지의 해시값을 생성하고 있습니다...");
                    String pHash = imageHashUtil.generatePHash(images[i]);
                    progressService.sendProgress(sessionId, "hash", "✅ " + (i + 1) + "번째 이미지 해시값 생성 완료: " + pHash.substring(0, 8) + "...");
                    
                    // 기존 DB와 중복 검사
                    progressService.sendProgress(sessionId, "hash", "🔍 " + (i + 1) + "번째 이미지의 DB 중복 검사 중...");
                    String duplicateHash = imageHashUtil.findDuplicateHash(pHash, existingHashes);
                    if (duplicateHash != null) {
                        String errorMsg = (i + 1) + "번째 이미지가 이미 DB에 존재하는 이미지와 유사합니다.";
                        progressService.sendError(sessionId, errorMsg);
                        return new IdolUploadResponseDto(false, errorMsg);
                    }
                    progressService.sendProgress(sessionId, "hash", "✅ " + (i + 1) + "번째 이미지 DB 중복 검사 통과");
                    
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
                    
                    // 일치하는 경우에만 즉시 DB에 저장
                    if (analysisResult.isMatch()) {
                        progressService.sendProgress(sessionId, "save", "💾 " + (i + 1) + "번째 이미지가 일치하므로 DB에 저장합니다...");
                        
                        IdolImage idolImage = new IdolImage(
                            idolName,
                            groupName,
                            result.getImageUrl(),
                            result.getpHash(),
                            result.getFileName(),
                            images[i].getSize(),
                            images[i].getContentType()
                        );
                        
                        idolImage.setGptAnalysis(result.getGptAnalysis());
                        idolImage.setIsVerified(true);
                        
                        IdolImage savedImage = idolImageRepository.save(idolImage);
                        validatedImages.add(savedImage);
                        
                        progressService.sendProgress(sessionId, "save", "✅ " + (i + 1) + "번째 이미지 DB 저장 완료 (ID: " + savedImage.getId() + ")");
                    } else {
                        progressService.sendProgress(sessionId, "validation", "❌ " + (i + 1) + "번째 이미지가 일치하지 않아 DB에 저장하지 않습니다.");
                    }
                    
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
     * 이미지 삭제
     */
    public boolean deleteImage(Long imageId) {
        try {
            idolImageRepository.deleteById(imageId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
