package com.example.gpt_test.repository;

import com.example.gpt_test.entity.IdolImage;
import com.example.gpt_test.entity.User;
import com.example.gpt_test.entity.GroupIdol;
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
     * 특정 사용자의 pHash 값으로 중복 이미지 검사
     */
    boolean existsByUserAndPHash(User user, String pHash);
    
    /**
     * 특정 아이돌의 모든 이미지 조회 (기존 호환성)
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
     * 특정 사용자의 모든 pHash 값 조회 (개인 갤러리 중복 검사용)
     */
    @Query("SELECT i.pHash FROM IdolImage i WHERE i.user = :user")
    List<String> findPHashesByUser(@Param("user") User user);
    
    // === 새로운 사용자별/그룹별 조회 메서드 ===
    
    /**
     * 사용자의 개인 갤러리 이미지 조회
     */
    List<IdolImage> findByUserAndIsInPersonalGalleryTrue(User user);
    
    /**
     * 사용자의 특정 아이돌 이미지 조회
     */
    List<IdolImage> findByUserAndIdolNameAndGroupNameAndIsInPersonalGalleryTrue(User user, String idolName, String groupName);
    
    /**
     * 그룹 DB의 공유 이미지 조회
     */
    List<IdolImage> findByGroupIdolAndIsInGroupDatabaseTrue(GroupIdol groupIdol);
    
    /**
     * 특정 그룹_아이돌의 모든 공유 이미지 조회
     */
    @Query("SELECT i FROM IdolImage i WHERE i.groupIdol.groupIdolKey = :groupIdolKey AND i.isInGroupDatabase = true")
    List<IdolImage> findGroupSharedImages(@Param("groupIdolKey") String groupIdolKey);
    
    /**
     * 사용자가 업로드한 모든 이미지 조회 (개인 갤러리 + 그룹 DB 포함)
     */
    List<IdolImage> findByUser(User user);
    
    /**
     * 사용자의 검증된 이미지만 조회
     */
    List<IdolImage> findByUserAndIsVerifiedTrue(User user);
    
    /**
     * 그룹 DB에 있는 검증된 이미지 조회
     */
    List<IdolImage> findByIsInGroupDatabaseTrueAndIsVerifiedTrue();
    
    /**
     * 특정 아이돌의 모든 이미지 조회 (업로더 정보 포함)
     */
    @Query("SELECT i FROM IdolImage i JOIN FETCH i.user WHERE i.idolName = :idolName AND i.groupName = :groupName ORDER BY i.uploadedAt DESC")
    List<IdolImage> findByIdolNameAndGroupNameWithUser(@Param("idolName") String idolName, @Param("groupName") String groupName);
}
