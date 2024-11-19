package techblog.repository.jpa;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import techblog.domain.BlogPost;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface BlogPostJpaRepository extends JpaRepository<BlogPost, String> {
    // 최근 게시물 조회
    List<BlogPost> findTop10ByOrderByPublishDateDesc();

    // 회사별 최근 게시물 조회
    List<BlogPost> findByCompanyOrderByPublishDateDesc(String company, Pageable pageable);

    // 특정 기간의 게시물 조회
    List<BlogPost> findByPublishDateBetweenOrderByPublishDateDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // 태그로 게시물 조회
    @Query("SELECT b FROM BlogPost b JOIN b.tags t WHERE t IN :tags ORDER BY b.publishDate DESC")
    List<BlogPost> findByTagsInOrderByPublishDateDesc(Set<String> tags, Pageable pageable);

    // 회사별 태그 통계
    @Query("SELECT b.company, t, COUNT(b) as cnt " +
            "FROM BlogPost b JOIN b.tags t " +
            "WHERE b.publishDate >= :startDate " +
            "GROUP BY b.company, t " +
            "ORDER BY cnt DESC")
    List<Object[]> findTagStatsByCompany(LocalDateTime startDate);

    // 키워드로 게시물 검색 (제목 + 내용)
    @Query("SELECT b FROM BlogPost b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY b.publishDate DESC")
    Page<BlogPost> searchByKeyword(String keyword, Pageable pageable);

    // 회사와 키워드로 게시물 검색
    @Query("SELECT b FROM BlogPost b WHERE " +
            "b.company IN :companies AND " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY b.publishDate DESC")
    Page<BlogPost> searchByCompaniesAndKeyword(
            List<String> companies,
            String keyword,
            Pageable pageable
    );

    // 특정 기간 내 가장 많은 태그
    @Query("SELECT t, COUNT(b) as cnt " +
            "FROM BlogPost b JOIN b.tags t " +
            "WHERE b.publishDate BETWEEN :startDate AND :endDate " +
            "GROUP BY t " +
            "ORDER BY cnt DESC")
    List<Object[]> findMostUsedTags(LocalDateTime startDate, LocalDateTime endDate);

    // 특정 회사의 최근 태그 트렌드
    @Query("SELECT t, COUNT(b) as cnt " +
            "FROM BlogPost b JOIN b.tags t " +
            "WHERE b.company = :company " +
            "AND b.publishDate >= :startDate " +
            "GROUP BY t " +
            "ORDER BY cnt DESC")
    List<Object[]> findCompanyTagTrends(String company, LocalDateTime startDate);

    // URL로 중복 체크
    boolean existsByUrl(String url);

    // 회사별 게시물 수 카운트
    @Query("SELECT b.company, COUNT(b) " +
            "FROM BlogPost b " +
            "WHERE b.publishDate >= :startDate " +
            "GROUP BY b.company")
    List<Object[]> countPostsByCompany(LocalDateTime startDate);
}
