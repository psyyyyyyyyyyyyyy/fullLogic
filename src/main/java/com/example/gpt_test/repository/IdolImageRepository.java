package com.example.gpt_test.repository;

import com.example.gpt_test.entity.IdolImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 아이돌 이미지 Repository
 */
@Repository
public interface IdolImageRepository extends JpaRepository<IdolImage, Long> {
    
    /**
     * pHash 값으로 중복 이미지 검사
     */
    boolean existsByPHash(String pHash);
    
    /**
     * 특정 아이돌의 모든 이미지 조회
     */
    List<IdolImage> findByIdolNameAndGroupName(String idolName, String groupName);
    
    /**
     * 검증된 이미지만 조회
     */
    List<IdolImage> findByIsVerifiedTrue();
    
    /**
     * pHash 값으로 이미지 조회
     */
    IdolImage findByPHash(String pHash);
    
    /**
     * 모든 pHash 값 조회 (중복 검사용)
     */
    @Query("SELECT i.pHash FROM IdolImage i")
    List<String> findAllPHashes();
}
