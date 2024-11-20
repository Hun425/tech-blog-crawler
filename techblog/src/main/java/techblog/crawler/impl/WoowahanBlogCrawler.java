package techblog.crawler.impl;

import com.rometools.rome.io.SyndFeedInput;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import techblog.crawler.RssFeedCrawler;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import techblog.crawler.WebCrawler;
import techblog.domain.BlogPost;

@Component
@Slf4j
public class WoowahanBlogCrawler extends WebCrawler {
    private WebDriver driver;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @PostConstruct
    public void init() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(TIMEOUT);
    }

    @Override
    protected String getBaseUrl() {
        return "https://techblog.woowahan.com/";
    }

    @Override
    protected String getListSelector() {
        return ".post-list .post-item";
    }

    @Override
    protected String getTitleSelector() {
        return "h1.headline";
    }

    @Override
    protected String getContentSelector() {
        return "div.entry-content";
    }

    @Override
    protected String getTagSelector() {
        return "a[rel=tag]";
    }

    @Override
    public List<BlogPost> crawl() {
        List<BlogPost> allPosts = new ArrayList<>();

        try {
            driver.get(getBaseUrl());
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".post-list")));

            List<Map<String, String>> postInfos = new ArrayList<>();
            for (WebElement element : driver.findElements(By.cssSelector(".post-item:not(.firstpaint)"))) {
                Map<String, String> info = new HashMap<>();
                info.put("url", element.findElement(By.cssSelector("a")).getAttribute("href"));
                info.put("title", element.findElement(By.cssSelector("h2.post-title")).getText());
                info.put("date", element.findElement(By.cssSelector("time.post-author-date")).getText());
                postInfos.add(info);
            }

            for (Map<String, String> info : postInfos) {
                try {
                    driver.get(info.get("url"));
                    // 수정된 선택자: .post-entry 또는 .content
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".content")));

                    // 전체 본문 내용 추출
                    WebElement contentElement = driver.findElement(By.cssSelector(".content"));
                    String content = extractCleanContent(contentElement);

                    BlogPost post = BlogPost.builder()
                            .title(info.get("title"))
                            .content(content)
                            .company(getCompanyName())
                            .url(info.get("url"))
                            .publishDate(parseDate(info.get("date")))
                            .tags(extractTags())
                            .build();

                    allPosts.add(post);
                    Thread.sleep(1000);

                    log.info("포스트 크롤링 완료: {}", info.get("title"));  // 로그 추가
                } catch (Exception e) {
                    log.error("포스트 크롤링 중 오류 - URL: {}, 오류: {}", info.get("url"), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("크롤링 중 오류: {}", e.getMessage());
        }

        return allPosts;
    }

    private String extractCleanContent(WebElement contentElement) {
        try {
            String htmlContent = contentElement.getAttribute("innerHTML");
            Document doc = Jsoup.parse(htmlContent);

            // 불필요한 요소 제거
            doc.select("script, style, iframe, .navigation, .share-wrap, .comments").remove();

            StringBuilder cleanContent = new StringBuilder();

            // 본문의 모든 텍스트 노드와 코드 블록을 순회하며 추출
            for (Element element : doc.select("p, h1, h2, h3, h4, h5, h6, pre, code, ul, ol, li")) {
                String text = element.text().trim();
                if (!text.isEmpty()) {
                    cleanContent.append("\n").append(text);
                }
            }

            return cleanContent.toString().trim()
                    .replaceAll("\n{3,}", "\n\n")
                    .replaceAll("\\s{2,}", " ");
        } catch (Exception e) {
            log.error("컨텐츠 정제 중 오류: {}", e.getMessage());
            return contentElement.getText();
        }
    }




    @Override
    public String getCompanyName() {
        return "우아한형제들";
    }

    @Override
    protected LocalDateTime extractPublishDate(Document doc) {
        return null;
    }

    private Set<String> extractTags() {
        return driver.findElements(By.cssSelector(".post-tags a")).stream()
                .map(WebElement::getText)
                .collect(Collectors.toSet());
    }

    private LocalDateTime parseDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM.dd.yyyy", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(dateStr, formatter);
        return date.atStartOfDay();
    }

    @PreDestroy
    public void cleanup() {
        if (driver != null) {
            driver.quit();
        }
    }
}