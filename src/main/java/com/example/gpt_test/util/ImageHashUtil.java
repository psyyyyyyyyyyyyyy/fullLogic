package com.example.gpt_test.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 이미지 해시 유틸리티 클래스
 */
@Component
public class ImageHashUtil {
    
    private static final int HASH_SIZE = 8; // 8x8 그리드로 pHash 생성
    
    /**
     * MultipartFile로부터 pHash 값을 생성
     */
    public String generatePHash(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IOException("이미지 파일을 읽을 수 없습니다: " + file.getOriginalFilename());
            }
            
            return generatePHash(image);
        }
    }
    
    /**
     * BufferedImage로부터 pHash 값을 생성
     * 간단한 pHash 알고리즘 구현
     */
    public String generatePHash(BufferedImage image) {
        // 1. 이미지를 32x32로 리사이즈 (DCT를 위해)
        BufferedImage resized = resizeImage(image, 32, 32);
        
        // 2. 그레이스케일로 변환
        BufferedImage grayscale = convertToGrayscale(resized);
        
        // 3. DCT (Discrete Cosine Transform) 적용 - 간단한 버전
        double[][] dctMatrix = applySimpleDCT(grayscale);
        
        // 4. 8x8 영역의 평균값 계산 (좌상단 8x8 영역 사용)
        double average = calculateAverage(dctMatrix, HASH_SIZE);
        
        // 5. 평균보다 큰 값은 1, 작은 값은 0으로 변환하여 해시 생성
        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < HASH_SIZE; i++) {
            for (int j = 0; j < HASH_SIZE; j++) {
                hash.append(dctMatrix[i][j] > average ? "1" : "0");
            }
        }
        
        // 6. 이진 문자열을 16진수로 변환
        return binaryToHex(hash.toString());
    }
    
    /**
     * 이미지 리사이즈
     */
    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, width, height, null);
        g2d.dispose();
        return resized;
    }
    
    /**
     * 그레이스케일로 변환
     */
    private BufferedImage convertToGrayscale(BufferedImage original) {
        BufferedImage grayscale = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayscale.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();
        return grayscale;
    }
    
    /**
     * 간단한 DCT 적용 (실제로는 평균 기반 해시)
     */
    private double[][] applySimpleDCT(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] matrix = new double[height][width];
        
        // 픽셀 값을 matrix에 저장
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color color = new Color(image.getRGB(j, i));
                matrix[i][j] = color.getRed(); // 그레이스케일이므로 R, G, B 값이 동일
            }
        }
        
        return matrix;
    }
    
    /**
     * 8x8 영역의 평균값 계산
     */
    private double calculateAverage(double[][] matrix, int size) {
        double sum = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sum += matrix[i][j];
            }
        }
        return sum / (size * size);
    }
    
    /**
     * 이진 문자열을 16진수로 변환
     */
    private String binaryToHex(String binary) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 4) {
            String fourBits = binary.substring(i, Math.min(i + 4, binary.length()));
            while (fourBits.length() < 4) {
                fourBits += "0"; // 패딩
            }
            int decimal = Integer.parseInt(fourBits, 2);
            hex.append(Integer.toHexString(decimal));
        }
        return hex.toString();
    }
    
    /**
     * 두 pHash 값의 유사도를 계산 (0.0 ~ 1.0, 1.0이 완전히 동일)
     */
    public double calculateSimilarity(String hash1, String hash2) {
        if (hash1 == null || hash2 == null) {
            return 0.0;
        }
        
        if (hash1.equals(hash2)) {
            return 1.0;
        }
        
        // Hamming distance 계산
        int distance = calculateHammingDistance(hash1, hash2);
        int maxDistance = Math.max(hash1.length(), hash2.length()) * 4; // 각 hex 문자는 4비트
        
        return 1.0 - (double) distance / maxDistance;
    }
    
    /**
     * 두 해시 값의 Hamming distance 계산
     */
    private int calculateHammingDistance(String hash1, String hash2) {
        if (hash1.length() != hash2.length()) {
            return Math.max(hash1.length(), hash2.length()) * 4;
        }
        
        int distance = 0;
        for (int i = 0; i < hash1.length(); i++) {
            char c1 = hash1.charAt(i);
            char c2 = hash2.charAt(i);
            
            if (c1 != c2) {
                // 16진수 문자를 4비트 이진수로 변환하여 비교
                int val1 = Character.digit(c1, 16);
                int val2 = Character.digit(c2, 16);
                int xor = val1 ^ val2;
                
                // 1비트의 개수를 세기
                distance += Integer.bitCount(xor);
            }
        }
        
        return distance;
    }
    
    /**
     * 주어진 pHash가 기존 해시 목록과 중복되는지 확인
     * @param newHash 새로운 해시값
     * @param existingHashes 기존 해시값 목록
     * @param threshold 유사도 임계값 (기본 0.95, 95% 이상 유사하면 중복으로 판단)
     * @return 중복되는 해시값이 있으면 해당 해시값, 없으면 null
     */
    public String findDuplicateHash(String newHash, List<String> existingHashes, double threshold) {
        for (String existingHash : existingHashes) {
            double similarity = calculateSimilarity(newHash, existingHash);
            if (similarity >= threshold) {
                return existingHash;
            }
        }
        return null;
    }
    
    /**
     * 기본 임계값(0.95)으로 중복 해시 찾기
     */
    public String findDuplicateHash(String newHash, List<String> existingHashes) {
        return findDuplicateHash(newHash, existingHashes, 0.95);
    }
    
    /**
     * 이미지 파일이 유효한지 검사
     */
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        return contentType.startsWith("image/") && 
               (contentType.equals("image/jpeg") || 
                contentType.equals("image/jpg") || 
                contentType.equals("image/png") || 
                contentType.equals("image/gif") || 
                contentType.equals("image/webp"));
    }
}
