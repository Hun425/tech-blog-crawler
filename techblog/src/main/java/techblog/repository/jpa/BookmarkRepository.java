package techblog.repository.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import techblog.domain.BlogPost;
import techblog.domain.Bookmark;
import techblog.domain.User;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    /**
     * 특정 사용자의 활성화된 북마크 목록 조회
     */
    @Query("SELECT b FROM Bookmark b WHERE b.user = :user AND b.deleted = false")
    Page<Bookmark> findByUserAndDeletedFalse(User user, Pageable pageable);

    /**
     * 특정 사용자의 특정 게시글 북마크 조회
     */
    @Query("SELECT b FROM Bookmark b " +
            "WHERE b.user = :user AND b.post = :post AND b.deleted = false")
    Optional<Bookmark> findByUserAndPostAndDeletedFalse(User user, BlogPost post);

    /**
     * 특정 사용자의 특정 게시글 북마크 존재 여부 확인
     */
    @Query("SELECT COUNT(b) > 0 FROM Bookmark b " +
            "WHERE b.user = :user AND b.post = :post AND b.deleted = false")
    boolean existsByUserAndPostAndDeletedFalse(User user, BlogPost post);

    /**
     * 특정 사용자의 특정 게시글 북마크 소프트 삭제
     */
    @Modifying
    @Query("UPDATE Bookmark b SET b.deleted = true " +
            "WHERE b.user = :user AND b.post = :post")
    void softDeleteByUserAndPost(User user, BlogPost post);

    /**
     * 특정 게시글의 북마크 수 조회
     */
    @Query("SELECT COUNT(b) FROM Bookmark b " +
            "WHERE b.post = :post AND b.deleted = false")
    Long countByPost(BlogPost post);

    /**
     * 특정 사용자의 북마크 수 조회
     */
    @Query("SELECT COUNT(b) FROM Bookmark b " +
            "WHERE b.user = :user AND b.deleted = false")
    Long countByUser(User user);
}