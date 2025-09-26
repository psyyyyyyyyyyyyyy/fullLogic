package com.example.gpt_test.service;

import com.example.gpt_test.entity.GroupIdol;
import com.example.gpt_test.repository.GroupIdolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 그룹_아이돌 관리 서비스
 */
@Service
public class GroupIdolService {
    
    @Autowired
    private GroupIdolRepository groupIdolRepository;
    
    /**
     * 그룹_아이돌 조회 또는 생성
     */
    public GroupIdol findOrCreateGroupIdol(String groupName, String idolName) {
        String groupIdolKey = GroupIdol.generateGroupIdolKey(groupName, idolName);
        
        Optional<GroupIdol> existing = groupIdolRepository.findByGroupIdolKey(groupIdolKey);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // 새로 생성
        GroupIdol newGroupIdol = new GroupIdol(groupName, idolName);
        return groupIdolRepository.save(newGroupIdol);
    }
    
    /**
     * 그룹_아이돌 키로 조회
     */
    public Optional<GroupIdol> findByGroupIdolKey(String groupIdolKey) {
        return groupIdolRepository.findByGroupIdolKey(groupIdolKey);
    }
    
    /**
     * 그룹명과 아이돌명으로 조회
     */
    public Optional<GroupIdol> findByGroupNameAndIdolName(String groupName, String idolName) {
        return groupIdolRepository.findByGroupNameAndIdolName(groupName, idolName);
    }
    
    /**
     * 그룹의 모든 아이돌 조회
     */
    public List<GroupIdol> findByGroupName(String groupName) {
        return groupIdolRepository.findByGroupName(groupName);
    }
    
    /**
     * 아이돌명으로 모든 그룹 조회
     */
    public List<GroupIdol> findByIdolName(String idolName) {
        return groupIdolRepository.findByIdolName(idolName);
    }
    
    /**
     * 모든 그룹_아이돌 조회
     */
    public List<GroupIdol> findAll() {
        return groupIdolRepository.findAll();
    }
    
    /**
     * 이미지 수 증가
     */
    public void incrementImageCount(GroupIdol groupIdol) {
        groupIdol.incrementImageCount();
        groupIdolRepository.save(groupIdol);
    }
    
    /**
     * 이미지 수 감소
     */
    public void decrementImageCount(GroupIdol groupIdol) {
        groupIdol.decrementImageCount();
        groupIdolRepository.save(groupIdol);
    }
}
