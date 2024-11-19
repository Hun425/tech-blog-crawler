package techblog.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import techblog.crawler.BlogCrawler;
import techblog.domain.BlogPost;
import techblog.domain.BlogPostDocument;
import techblog.repository.jpa.BlogPostJpaRepository;

import java.util.List;
@Tag(name = "크롤링 컨트롤러", description = "기술 블로그 크롤링 관련 API")
@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlingService {
    private final List<BlogCrawler> crawlers;
    private final BlogPostJpaRepository blogPostRepository;
    private final ElasticsearchClient elasticsearchClient;

    private static final long Month_IN_DAYS = 31;
    @Scheduled(cron = "0 0 */4 * * *")  // 4시간마다 실행
    @Transactional
    public void crawlAllBlogs() {
        log.info("기술 블로그 크롤링 시작");

        for (BlogCrawler crawler : crawlers) {
            try {
                log.info("{} 블로그 크롤링 시작", crawler.getCompanyName());
                List<BlogPost> posts = crawler.crawl();

                for (BlogPost post : posts) {
                    if (!blogPostRepository.existsByUrl(post.getUrl())) {
                        blogPostRepository.save(post);
                        indexToElasticsearch(post);
                        log.info("새 포스트 저장: {}", post.getTitle());
                    }
                }

                log.info("{} 블로그 크롤링 완료 - {} 개의 포스트",
                        crawler.getCompanyName(), posts.size());
            } catch (Exception e) {
                log.error("{} 블로그 크롤링 중 오류 발생: {}",
                        crawler.getCompanyName(), e.getMessage(), e);
            }
        }

        log.info("모든 블로그 크롤링 완료");
    }

    private void indexToElasticsearch(BlogPost post) {
        try {
            BlogPostDocument document = BlogPostDocument.from(post);
            IndexRequest<BlogPostDocument> request = IndexRequest.of(i -> i
                    .index("blog-posts")
                    .id(document.getId())
                    .document(document)
            );
            elasticsearchClient.index(request);
        } catch (Exception e) {
            log.error("Elasticsearch 인덱싱 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
