package com.example.gpt_test.controller;

import com.example.gpt_test.dto.auth.AuthResponseDto;
import com.example.gpt_test.dto.auth.LoginRequestDto;
import com.example.gpt_test.dto.auth.RegisterRequestDto;
import com.example.gpt_test.entity.User;
import com.example.gpt_test.service.JwtService;
import com.example.gpt_test.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 인증 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "사용자 인증 관련 API")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * 회원가입
     */
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        try {
            User user = userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
            String token = jwtService.generateToken(user.getUsername());
            
            AuthResponseDto response = new AuthResponseDto(
                true, 
                "회원가입이 성공적으로 완료되었습니다.", 
                token, 
                user.getUsername(), 
                user.getEmail()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new AuthResponseDto(false, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                new AuthResponseDto(false, "회원가입 중 오류가 발생했습니다.")
            );
        }
    }
    
    /**
     * 로그인
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            Optional<User> userOpt = userService.authenticateUser(request.getUsername(), request.getPassword());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String token = jwtService.generateToken(user.getUsername());
                
                AuthResponseDto response = new AuthResponseDto(
                    true, 
                    "로그인이 성공적으로 완료되었습니다.", 
                    token, 
                    user.getUsername(), 
                    user.getEmail()
                );
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(
                    new AuthResponseDto(false, "사용자명 또는 비밀번호가 올바르지 않습니다.")
                );
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                new AuthResponseDto(false, "로그인 중 오류가 발생했습니다.")
            );
        }
    }
    
    /**
     * 토큰 검증
     */
    @GetMapping("/validate")
    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증합니다.")
    public ResponseEntity<AuthResponseDto> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                    new AuthResponseDto(false, "유효하지 않은 토큰 형식입니다.")
                );
            }
            
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (jwtService.isTokenValid(token, user)) {
                    return ResponseEntity.ok(
                        new AuthResponseDto(true, "유효한 토큰입니다.", token, user.getUsername(), user.getEmail())
                    );
                }
            }
            
            return ResponseEntity.badRequest().body(
                new AuthResponseDto(false, "유효하지 않은 토큰입니다.")
            );
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new AuthResponseDto(false, "토큰 검증 중 오류가 발생했습니다.")
            );
        }
    }
}
