package com.example.gpt_test.repository;

import com.example.gpt_test.entity.GroupIdol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 그룹_아이돌 레포지토리
 */
@Repository
public interface GroupIdolRepository extends JpaRepository<GroupIdol, Long> {
    
    /**
     * 그룹명과 아이돌명으로 조회
     */
    Optional<GroupIdol> findByGroupNameAndIdolName(String groupName, String idolName);
    
    /**
     * 그룹_아이돌 키로 조회
     */
    Optional<GroupIdol> findByGroupIdolKey(String groupIdolKey);
    
    /**
     * 그룹명으로 모든 아이돌 조회
     */
    List<GroupIdol> findByGroupName(String groupName);
    
    /**
     * 아이돌명으로 모든 그룹 조회
     */
    List<GroupIdol> findByIdolName(String idolName);
    
    /**
     * 그룹_아이돌 키 존재 여부 확인
     */
    boolean existsByGroupIdolKey(String groupIdolKey);
}
