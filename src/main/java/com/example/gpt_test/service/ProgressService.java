package com.example.gpt_test.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 진행 상황을 실시간으로 전송하는 서비스
 */
@Service
public class ProgressService {
    
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    
    /**
     * SSE 연결 추가
     */
    public SseEmitter addEmitter(String sessionId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5분 타임아웃
        
        emitters.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        emitter.onCompletion(() -> removeEmitter(sessionId, emitter));
        emitter.onTimeout(() -> removeEmitter(sessionId, emitter));
        emitter.onError((ex) -> removeEmitter(sessionId, emitter));
        
        return emitter;
    }
    
    /**
     * SSE 연결 제거
     */
    private void removeEmitter(String sessionId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> sessionEmitters = emitters.get(sessionId);
        if (sessionEmitters != null) {
            sessionEmitters.remove(emitter);
            if (sessionEmitters.isEmpty()) {
                emitters.remove(sessionId);
            }
        }
    }
    
    /**
     * 진행 상황 메시지 전송
     */
    public void sendProgress(String sessionId, String step, String message) {
        sendProgress(sessionId, step, message, null);
    }
    
    /**
     * 진행 상황 메시지 전송 (데이터 포함)
     */
    public void sendProgress(String sessionId, String step, String message, Object data) {
        CopyOnWriteArrayList<SseEmitter> sessionEmitters = emitters.get(sessionId);
        if (sessionEmitters != null && !sessionEmitters.isEmpty()) {
            ProgressMessage progressMessage = new ProgressMessage(step, message, data, System.currentTimeMillis());
            
            sessionEmitters.removeIf(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(progressMessage));
                    return false;
                } catch (IOException | IllegalStateException e) {
                    // 연결이 끊어졌거나 이미 완료된 emitter 제거
                    System.out.println("SSE 연결 오류로 emitter 제거: " + e.getMessage());
                    return true;
                }
            });
            
            // 모든 emitter가 제거되었으면 세션도 제거
            if (sessionEmitters.isEmpty()) {
                emitters.remove(sessionId);
            }
        }
    }
    
    /**
     * 완료 메시지 전송
     */
    public void sendComplete(String sessionId, Object result) {
        CopyOnWriteArrayList<SseEmitter> sessionEmitters = emitters.get(sessionId);
        if (sessionEmitters != null && !sessionEmitters.isEmpty()) {
            sessionEmitters.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(result));
                    emitter.complete();
                } catch (IOException | IllegalStateException e) {
                    System.out.println("SSE 완료 메시지 전송 오류: " + e.getMessage());
                    // emitter가 이미 완료되었거나 연결이 끊어진 경우
                }
            });
            emitters.remove(sessionId);
        }
    }
    
    /**
     * 오류 메시지 전송
     */
    public void sendError(String sessionId, String error) {
        CopyOnWriteArrayList<SseEmitter> sessionEmitters = emitters.get(sessionId);
        if (sessionEmitters != null && !sessionEmitters.isEmpty()) {
            sessionEmitters.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data(error));
                    emitter.complete();
                } catch (IOException | IllegalStateException e) {
                    System.out.println("SSE 오류 메시지 전송 오류: " + e.getMessage());
                    // emitter가 이미 완료되었거나 연결이 끊어진 경우
                }
            });
            emitters.remove(sessionId);
        }
    }
    
    /**
     * 진행 상황 메시지 클래스
     */
    public static class ProgressMessage {
        private String step;
        private String message;
        private Object data;
        private long timestamp;
        
        public ProgressMessage(String step, String message, Object data, long timestamp) {
            this.step = step;
            this.message = message;
            this.data = data;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getStep() { return step; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
        public long getTimestamp() { return timestamp; }
    }
}
