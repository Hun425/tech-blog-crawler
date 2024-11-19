package techblog.crawler;

import techblog.domain.BlogPost;

import java.util.List;

public interface BlogCrawler {
    List<BlogPost> crawl();
    String getCompanyName();
}
