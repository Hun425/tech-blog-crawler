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

            // 포스트 목록이 로드될 때까지 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".post-list")));

            // 모든 포스트 정보를 한번에 수집
            List<Map<String, String>> postInfos = new ArrayList<>();
            for (WebElement element : driver.findElements(By.cssSelector(".post-item:not(.firstpaint)"))) {
                Map<String, String> info = new HashMap<>();
                info.put("url", element.findElement(By.cssSelector("a")).getAttribute("href"));
                info.put("title", element.findElement(By.cssSelector("h2.post-title")).getText());
                info.put("date", element.findElement(By.cssSelector("time.post-author-date")).getText());
                postInfos.add(info);
            }

            // 각 포스트 상세 페이지 방문
            for (Map<String, String> info : postInfos) {
                try {
                    driver.get(info.get("url"));
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.content")));

                    BlogPost post = BlogPost.builder()
                            .title(info.get("title"))
                            .content(driver.findElement(By.cssSelector("div.content")).getAttribute("innerHTML"))
                            .company(getCompanyName())
                            .url(info.get("url"))
                            .publishDate(parseDate(info.get("date")))
                            .tags(extractTags())
                            .build();

                    allPosts.add(post);
                    Thread.sleep(1000); // 요청 간격
                } catch (Exception e) {
                    log.error("포스트 크롤링 중 오류: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("크롤링 중 오류: {}", e.getMessage());
        }

        return allPosts;
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