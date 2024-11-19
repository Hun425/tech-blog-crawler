package techblog.crawler;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import techblog.domain.BlogPost;

import java.net.URL;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public abstract class RssFeedCrawler implements BlogCrawler {

    private final SyndFeedInput feedInput;
    private static final int MAX_RETRIES = 1;
    protected abstract String getFeedUrl();

    @Autowired  // 생성자 주입을 위한 어노테이션 추가
    protected RssFeedCrawler(SyndFeedInput feedInput) {
        this.feedInput = feedInput;
    }

    @Override
    public List<BlogPost> crawl() {
        try {
            URL feedUrl = new URL(getFeedUrl());
            List<BlogPost> posts = new ArrayList<>();

            for (int i = 0; i < MAX_RETRIES; i++) {
                try (XmlReader reader = new XmlReader(feedUrl)) {
                    SyndFeed feed = feedInput.build(reader);
                    log.info("{} - 총 게시글 수: {}", getCompanyName(), feed.getEntries().size());

                    posts = feed.getEntries().stream()
                            .map(this::convertToPost)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    break;  // 성공하면 루프 종료
                } catch (Exception e) {
                    if (i == MAX_RETRIES - 1) {
                        throw e;  // 마지막 시도에서 실패하면 예외 던짐
                    }
                    log.warn("{}번째 시도 실패, 재시도 중...", i + 1);
                    Thread.sleep(1000);  // 1초 대기 후 재시도
                }
            }

            log.info("{} - 변환된 게시글 수: {}", getCompanyName(), posts.size());
            return posts;
        } catch (Exception e) {
            log.error("크롤링 중 오류 발생 - {}: {}", getCompanyName(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    protected BlogPost convertToPost(SyndEntry entry) {
        try {
            // HTML에서 태그 추출
            Set<String> tags = extractTags(entry.getDescription().getValue());

            return BlogPost.builder()
                    .title(entry.getTitle())
                    .content(entry.getDescription().getValue())
                    .company(getCompanyName())
                    .url(entry.getLink())
                    .publishDate(entry.getPublishedDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime())
                    .tags(tags)
                    .build();
        } catch (Exception e) {
            log.error("포스트 변환 중 오류 발생: {}", entry.getTitle(), e);
            return null;
        }
    }

    protected Set<String> extractTags(String html) {
        try {
            // JSoup을 사용하여 HTML에서 태그 추출
            Document doc = Jsoup.parse(html);
            Elements tagElements = doc.select("a[href*=tag], a[href*=category]");

            Set<String> tags = new HashSet<>();
            for (Element element : tagElements) {
                String tag = element.text().trim().toLowerCase();
                if (!tag.isEmpty()) {
                    tags.add(tag);
                }
            }
            return tags;
        } catch (Exception e) {
            log.error("태그 추출 중 오류 발생: {}", e.getMessage(), e);
            return new HashSet<>();
        }
    }
}
