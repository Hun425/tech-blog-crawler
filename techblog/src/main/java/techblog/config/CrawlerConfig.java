package techblog.config;

import com.rometools.rome.io.SyndFeedInput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrawlerConfig {

    @Bean
    public SyndFeedInput syndFeedInput() {
        return new SyndFeedInput();
    }
}
