package techblog.repository.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import techblog.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 이메일로 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String username);

    /**
     * 이메일 존재 여부 확인
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.nickname = :nickname")
    boolean existsByNickname(String nickname);

    /**
     * 특정 사용자를 제외한 닉네임 중복 확인 (프로필 수정 시 사용)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.nickname = :nickname AND u.id != :userId")
    boolean existsByNicknameAndIdNot(String nickname, Long userId);

    /**
     * 특정 날짜 이후 가입한 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    Long countByCreatedAtAfter(LocalDateTime date);


    /**
     * 특정 태그에 관심이 있는 사용자 목록 조회
     */
    @Query("SELECT u FROM User u JOIN u.interestedTags t WHERE t = :tag")
    List<User> findByInterestedTag(String tag, Pageable pageable);
}