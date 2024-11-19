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
@RequiredArgsConstructor
@Slf4j
public abstract class RssFeedCrawler implements BlogCrawler {

    private final SyndFeedInput feedInput;

    protected abstract String getFeedUrl();

    @Override
    public List<BlogPost> crawl() {
        try {
            URL feedUrl = new URL(getFeedUrl());
            try (XmlReader reader = new XmlReader(feedUrl)) {
                SyndFeed feed = feedInput.build(reader);
                return feed.getEntries().stream()
                        .map(this::convertToPost)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
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
