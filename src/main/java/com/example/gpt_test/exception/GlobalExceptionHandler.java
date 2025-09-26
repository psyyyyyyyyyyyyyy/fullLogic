package com.example.gpt_test.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 클래스
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 입력 검증 예외 처리
     * 
     * @param ex 검증 예외
     * @return 검증 오류 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("status", "error");
        response.put("code", 400);
        response.put("message", "입력 값 검증에 실패했습니다.");
        response.put("errors", errors);
        response.put("timestamp", System.currentTimeMillis());
        
        logger.warn("입력 검증 실패: {}", errors);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * REST API 호출 예외 처리 (GPT API 호출 실패 등)
     * 
     * @param ex REST 클라이언트 예외
     * @return 오류 응답
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleRestClientException(RestClientException ex) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("status", "error");
        response.put("code", 502);
        response.put("message", "외부 서비스와의 통신에 실패했습니다.");
        response.put("timestamp", System.currentTimeMillis());
        
        logger.error("외부 API 호출 실패: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }
    
    /**
     * 네트워크 연결 예외 처리
     * 
     * @param ex 리소스 접근 예외
     * @return 오류 응답
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccessException(ResourceAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("status", "error");
        response.put("code", 504);
        response.put("message", "외부 서비스 연결 시간이 초과되었습니다.");
        response.put("timestamp", System.currentTimeMillis());
        
        logger.error("외부 서비스 연결 타임아웃: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }
    
    /**
     * 일반적인 예외 처리
     * 
     * @param ex 일반 예외
     * @return 오류 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("status", "error");
        response.put("code", 500);
        response.put("message", "서버 내부 오류가 발생했습니다.");
        response.put("timestamp", System.currentTimeMillis());
        
        logger.error("예상치 못한 오류 발생: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

