package techblog.crawler.impl;

import com.rometools.rome.io.SyndFeedInput;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import techblog.crawler.RssFeedCrawler;

@Component
public class WoowahanBlogCrawler extends RssFeedCrawler {

    @Autowired
    public WoowahanBlogCrawler(SyndFeedInput feedInput) {
        super(feedInput);  // 부모 클래스의 생성자 호출
    }

    @Override
    protected String getFeedUrl() {
        return "https://woowabros.github.io/feed.xml";
    }

    @Override
    public String getCompanyName() {
        return "우아한형제들";
    }
}
