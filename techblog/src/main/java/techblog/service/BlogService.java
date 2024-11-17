package techblog.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import techblog.domain.BlogPost;
import techblog.dto.request.BlogSearchRequest;
import techblog.dto.response.BlogPostResponse;
import techblog.dto.response.TrendResponse;
import techblog.repository.BlogPostRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 검색 쿼리 관련 추가 import
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService {
    private final BlogPostRepository blogPostRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final RedisTemplate<String, String> redisTemplate;

    @Cacheable(value = "recentPosts", key = "#size")
    public List<BlogPostResponse> getRecentPosts(int size) {
        return blogPostRepository.findTop10ByOrderByPublishDateDesc()
                .stream()
                .map(BlogPostResponse::from)
                .collect(Collectors.toList());
    }

    public Page<BlogPostResponse> searchPosts(BlogSearchRequest request, Pageable pageable) {
        if (!hasSearchConditions(request)) {
            return blogPostRepository.findAll(pageable).map(BlogPostResponse::from);
        }

        try {
            var searchRequest = SearchRequest.of(s -> s
                    .index("blog-posts")
                    .from(pageable.getPageNumber() * pageable.getPageSize())
                    .size(pageable.getPageSize())
                    .sort(createSortOptions())
                    .query(q -> {
                        // Bool 쿼리 빌더 생성
                        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

                        // 키워드 검색
                        if (StringUtils.hasText(request.keyword())) {
                            boolBuilder.must(must -> must
                                    .multiMatch(m -> m
                                            .fields("title^2", "content")
                                            .query(request.keyword())
                                            .type(TextQueryType.BestFields)
                                    )
                            );
                        }

                        // 회사 필터
                        if (!CollectionUtils.isEmpty(request.companies())) {
                            boolBuilder.filter(f -> f
                                    .terms(t -> t
                                            .field("company")
                                            .terms(t2 -> t2
                                                    .value(request.companies().stream()
                                                            .map(FieldValue::of)
                                                            .collect(Collectors.toList()))
                                            )
                                    )
                            );
                        }

                        // 태그 필터
                        if (!CollectionUtils.isEmpty(request.tags())) {
                            boolBuilder.filter(f -> f
                                    .terms(t -> t
                                            .field("tags")
                                            .terms(t2 -> t2
                                                    .value(request.tags().stream()
                                                            .map(FieldValue::of)
                                                            .collect(Collectors.toList()))
                                            )
                                    )
                            );
                        }

                        // 날짜 범위 필터
                        if (request.startDate() != null && request.endDate() != null) {
                            boolBuilder.filter(f -> f
                                    .range(r -> r
                                            .field("publishDate")
                                            .gte(JsonData.of(request.startDate().toString()))
                                            .lte(JsonData.of(request.endDate().toString()))
                                    )
                            );
                        }

                        return q.bool(boolBuilder.build());
                    })
            );

            // 검색 실행
            SearchResponse<BlogPost> response = elasticsearchClient.search(searchRequest, BlogPost.class);

            // 결과 변환
            List<BlogPostResponse> blogPosts = response.hits().hits().stream()
                    .map(hit -> BlogPostResponse.from(hit.source()))
                    .collect(Collectors.toList());

            // 전체 결과 수
            long total = response.hits().total().value();

            return new PageImpl<>(blogPosts, pageable, total);

        } catch (IOException e) {
            log.error("Error while searching posts", e);
            throw new RuntimeException("Search failed", e);
        }
    }



    @Cacheable(value = "trends", key = "'weekly'")
    public TrendResponse getWeeklyTrends() {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusWeeks(1);

            // Aggregation 쿼리 생성
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                    .index("blog-posts")
                    .size(0) // aggregation만 필요하므로 hits는 불필요
                    .aggregations("tags", a -> a
                            .terms(t -> t
                                    .field("tags")
                                    .size(20)
                            )
                            .aggregations("weekly", a2 -> a2
                                    .dateHistogram(h -> h
                                            .field("publishDate")
                                            .calendarInterval(CalendarInterval.Week)
                                    )
                            )
                    )
                    .query(q -> q
                            .range(r -> r
                                    .field("publishDate")
                                    .from(startDate.toString())
                            )
                    );

            SearchResponse<BlogPost> response = elasticsearchClient.search(
                    searchBuilder.build(),
                    BlogPost.class
            );

            // 결과 처리
            List<TrendResponse.KeywordTrend> keywordTrends = processTagAggregations(response);
            Map<String, List<String>> companyTrends = aggregateCompanyTrends(startDate);

            return new TrendResponse(keywordTrends, companyTrends, LocalDateTime.now());

        } catch (IOException e) {
            log.error("Error while getting trends", e);
            throw new RuntimeException("Failed to get trends", e);
        }
    }

    private List<SortOptions> createSortOptions() {
        return List.of(
                SortOptions.of(s -> s
                        .field(FieldSort.of(f -> f
                                .field("publishDate")
                                .order(SortOrder.Desc)
                        )))
        );
    }

    private List<TrendResponse.KeywordTrend> processTagAggregations(SearchResponse<BlogPost> response) {
        var tagsAggregation = response.aggregations().get("tags");

        return tagsAggregation.sterms().buckets().array().stream()
                .map(bucket -> {
                    String tag = bucket.key()._get().toString();  // key 추출 방식 수정
                    long count = bucket.docCount();
                    double growthRate = calculateGrowthRate(bucket);
                    return new TrendResponse.KeywordTrend(
                            tag,
                            count,
                            growthRate,
                            findRelatedTags(tag)
                    );
                })
                .collect(Collectors.toList());
    }


    private double calculateGrowthRate(StringTermsBucket bucket) {
        var weeklyBuckets = bucket.aggregations()
                .get("weekly")
                .dateHistogram()
                .buckets()
                .array();

        if (weeklyBuckets.size() < 2) return 0.0;  // length 대신 size() 사용

        var currentBucket = weeklyBuckets.get(weeklyBuckets.size() - 1);  // array 인덱싱 대신 get() 사용
        var previousBucket = weeklyBuckets.get(weeklyBuckets.size() - 2);

        long currentCount = currentBucket.docCount();  // docCount는 그대로 사용 가능
        long previousCount = previousBucket.docCount();

        if (previousCount == 0) return 100.0;
        return ((currentCount - previousCount) / (double) previousCount) * 100;
    }

    private List<String> findRelatedTags(String tag) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("blog-posts")
                    .size(0)
                    .query(q -> q
                            .term(t -> t
                                    .field("tags")
                                    .value(tag)
                            )
                    )
                    .aggregations("related_tags", a -> a
                            .terms(t -> t
                                    .field("tags")
                                    .size(6)
                                    .exclude(e -> e.terms(List.of(tag))) // TermsExclude 대신 직접 exclude 설정
                            )
                    )
            );

            SearchResponse<BlogPost> response = elasticsearchClient.search(searchRequest, BlogPost.class);

            return response.aggregations()
                    .get("related_tags")
                    .sterms()
                    .buckets()
                    .array()
                    .stream()
                    .map(bucket -> bucket.key()._get().toString())  // key 추출 방식 수정
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Error while finding related tags", e);
            throw new RuntimeException("Failed to find related tags", e);
        }
    }

    private Map<String, List<String>> aggregateCompanyTrends(LocalDateTime startDate) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("blog-posts")
                    .size(0)
                    .query(q -> q
                            .range(r -> r
                                    .field("publishDate")
                                    .from(startDate.toString())
                            )
                    )
                    .aggregations("companies", a -> a
                            .terms(t -> t
                                    .field("company")
                                    .size(20)
                            )
                    )
            );

            SearchResponse<BlogPost> response = elasticsearchClient.search(searchRequest, BlogPost.class);

            Map<String, List<String>> companyTrends = new HashMap<>();
            var companyBuckets = response.aggregations()
                    .get("companies")
                    .sterms()
                    .buckets()
                    .array();

            for (var bucket : companyBuckets) {
                String company = bucket.key()._get().toString();  // key 추출 방식 수정
                List<String> posts = bucket.docCount() > 0
                        ? findCompanyPosts(company)
                        : List.of();
                companyTrends.put(company, posts);
            }

            return companyTrends;

        } catch (IOException e) {
            log.error("Error while aggregating company trends", e);
            throw new RuntimeException("Failed to aggregate company trends", e);
        }
    }

    private List<String> findCompanyPosts(String company) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("blog-posts")
                    .query(q -> q
                            .term(t -> t
                                    .field("company")
                                    .value(v -> v.stringValue(company))
                            )
                    )
            );

            SearchResponse<BlogPost> response = elasticsearchClient.search(searchRequest, BlogPost.class);

            return response.hits().hits().stream()
                    .map(hit -> hit.source().getTitle()) // 제목을 추출하여 반환
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Error while fetching company posts", e);
            return List.of();
        }
    }

    private boolean hasSearchConditions(BlogSearchRequest request) {
        return StringUtils.hasText(request.keyword()) ||
                !CollectionUtils.isEmpty(request.companies()) ||
                !CollectionUtils.isEmpty(request.tags()) ||
                request.startDate() != null ||
                request.endDate() != null;
    }
}
