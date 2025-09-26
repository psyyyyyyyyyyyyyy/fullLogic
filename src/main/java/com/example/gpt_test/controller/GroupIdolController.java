package com.example.gpt_test.controller;

import com.example.gpt_test.entity.GroupIdol;
import com.example.gpt_test.service.GroupIdolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 그룹_아이돌 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/group-idol")
@Tag(name = "Group Idol Management", description = "그룹_아이돌 관리 API")
public class GroupIdolController {
    
    @Autowired
    private GroupIdolService groupIdolService;
    
    /**
     * 모든 그룹_아이돌 조회
     */
    @GetMapping("/all")
    @Operation(summary = "모든 그룹_아이돌 조회", description = "등록된 모든 그룹_아이돌을 조회합니다.")
    public ResponseEntity<List<GroupIdol>> getAllGroupIdols() {
        List<GroupIdol> groupIdols = groupIdolService.findAll();
        return ResponseEntity.ok(groupIdols);
    }
    
    /**
     * 특정 그룹의 모든 아이돌 조회
     */
    @GetMapping("/by-group")
    @Operation(summary = "그룹별 아이돌 조회", description = "특정 그룹의 모든 아이돌을 조회합니다.")
    public ResponseEntity<List<GroupIdol>> getIdolsByGroup(
            @Parameter(description = "그룹명", required = true)
            @RequestParam @NotBlank String groupName) {
        
        List<GroupIdol> groupIdols = groupIdolService.findByGroupName(groupName);
        return ResponseEntity.ok(groupIdols);
    }
    
    /**
     * 특정 아이돌명으로 모든 그룹 조회
     */
    @GetMapping("/by-idol")
    @Operation(summary = "아이돌별 그룹 조회", description = "특정 아이돌명으로 모든 그룹을 조회합니다.")
    public ResponseEntity<List<GroupIdol>> getGroupsByIdol(
            @Parameter(description = "아이돌 이름", required = true)
            @RequestParam @NotBlank String idolName) {
        
        List<GroupIdol> groupIdols = groupIdolService.findByIdolName(idolName);
        return ResponseEntity.ok(groupIdols);
    }
    
    /**
     * 특정 그룹_아이돌 조회
     */
    @GetMapping("/specific")
    @Operation(summary = "특정 그룹_아이돌 조회", description = "그룹명과 아이돌명으로 특정 그룹_아이돌을 조회합니다.")
    public ResponseEntity<GroupIdol> getSpecificGroupIdol(
            @Parameter(description = "그룹명", required = true)
            @RequestParam @NotBlank String groupName,
            
            @Parameter(description = "아이돌 이름", required = true)
            @RequestParam @NotBlank String idolName) {
        
        Optional<GroupIdol> groupIdol = groupIdolService.findByGroupNameAndIdolName(groupName, idolName);
        
        if (groupIdol.isPresent()) {
            return ResponseEntity.ok(groupIdol.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 그룹_아이돌 키로 조회
     */
    @GetMapping("/by-key/{groupIdolKey}")
    @Operation(summary = "키로 그룹_아이돌 조회", description = "그룹_아이돌 키로 조회합니다.")
    public ResponseEntity<GroupIdol> getGroupIdolByKey(
            @Parameter(description = "그룹_아이돌 키 (그룹명_아이돌명)", required = true)
            @PathVariable String groupIdolKey) {
        
        Optional<GroupIdol> groupIdol = groupIdolService.findByGroupIdolKey(groupIdolKey);
        
        if (groupIdol.isPresent()) {
            return ResponseEntity.ok(groupIdol.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 그룹_아이돌 생성 또는 조회
     */
    @PostMapping("/find-or-create")
    @Operation(summary = "그룹_아이돌 생성 또는 조회", description = "그룹_아이돌을 조회하거나 없으면 새로 생성합니다.")
    public ResponseEntity<GroupIdol> findOrCreateGroupIdol(
            @Parameter(description = "그룹명", required = true)
            @RequestParam @NotBlank String groupName,
            
            @Parameter(description = "아이돌 이름", required = true)
            @RequestParam @NotBlank String idolName) {
        
        GroupIdol groupIdol = groupIdolService.findOrCreateGroupIdol(groupName, idolName);
        return ResponseEntity.ok(groupIdol);
    }
}
