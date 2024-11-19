package techblog.repository.elasticsearch;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import techblog.domain.BlogPost;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface BlogPostSearchRepository extends ElasticsearchRepository<BlogPost, String> {

    // 전문 검색 (제목, 내용)
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\",\"fields\": [\"title^2\", \"content\"]}}]}}")
    SearchHits<BlogPost> fullTextSearch(String query, Pageable pageable);

    // 회사별 전문 검색
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\",\"fields\": [\"title^2\", \"content\"]}},{\"terms\": {\"company\": ?1}}]}}")
    SearchHits<BlogPost> searchByKeywordAndCompanies(String keyword, List<String> companies, Pageable pageable);

    // 태그 기반 검색
    @Query("{\"bool\": {\"must\": [{\"terms\": {\"tags\": ?0}}]}}")
    SearchHits<BlogPost> searchByTags(List<String> tags, Pageable pageable);

    // 기간별 검색
    @Query("{\"bool\": {\"must\": [{\"range\": {\"publishDate\": {\"gte\": \"?0\",\"lte\": \"?1\"}}}]}}")
    SearchHits<BlogPost> searchByDateRange(String startDate, String endDate, Pageable pageable);

    // 복합 검색 (키워드 + 회사 + 태그 + 기간)
    @Query("{\"bool\": {\"must\": [" +
            "{\"multi_match\": {\"query\": \"?0\",\"fields\": [\"title^2\", \"content\"]}}," +
            "{\"terms\": {\"company\": ?1}}," +
            "{\"terms\": {\"tags\": ?2}}," +
            "{\"range\": {\"publishDate\": {\"gte\": \"?3\",\"lte\": \"?4\"}}}" +
            "]}}")
    SearchHits<BlogPost> complexSearch(
            String keyword,
            List<String> companies,
            List<String> tags,
            String startDate,
            String endDate,
            Pageable pageable
    );

    // 연관 게시물 검색
    @Query("{\"more_like_this\": {\"fields\": [\"title\",\"content\",\"tags\"],\"like\": [{\"_id\": \"?0\"}],\"min_term_freq\": 1,\"max_query_terms\": 12}}")
    SearchHits<BlogPost> findSimilarPosts(String postId, Pageable pageable);
}