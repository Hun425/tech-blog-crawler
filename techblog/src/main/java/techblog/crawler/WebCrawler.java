package techblog.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import techblog.domain.BlogPost;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public abstract class WebCrawler implements BlogCrawler {
    protected abstract String getBaseUrl();
    protected abstract String getListSelector();
    protected abstract String getTitleSelector();
    protected abstract String getContentSelector();
    protected abstract String getTagSelector();

    @Override
    public List<BlogPost> crawl() {
        try {
            Document doc = Jsoup.connect(getBaseUrl())
                    .userAgent("Mozilla/5.0")
                    .get();

            return doc.select(getListSelector()).stream()
                    .map(this::crawlPost)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("크롤링 중 오류 발생 - {}: {}", getCompanyName(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    protected BlogPost crawlPost(Element element) {
        try {
            String url = element.select("a").first().attr("abs:href");
            Document postDoc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();

            return BlogPost.builder()
                    .title(postDoc.select(getTitleSelector()).text())
                    .content(postDoc.select(getContentSelector()).text())
                    .company(getCompanyName())
                    .url(url)
                    .publishDate(extractPublishDate(postDoc))
                    .tags(extractTags(postDoc))
                    .build();
        } catch (Exception e) {
            log.error("포스트 크롤링 중 오류 발생: {}", element.text(), e);
            return null;
        }
    }

    protected abstract LocalDateTime extractPublishDate(Document doc);

    protected Set<String> extractTags(Document doc) {
        return doc.select(getTagSelector())
                .stream()
                .map(Element::text)
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
