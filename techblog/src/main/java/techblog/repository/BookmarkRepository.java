package techblog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import techblog.domain.Bookmark;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUserId(Long userId);
    Optional<Bookmark> findByUserIdAndPostId(Long userId, String postId);
    boolean existsByUserIdAndPostId(Long userId, String postId);
    void deleteByUserIdAndPostId(Long userId, String postId);
}