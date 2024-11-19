package techblog.repository;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "techblog.repository.jpa")
@EnableElasticsearchRepositories(basePackages = "techblog.repository.elasticsearch")
public class RepositoryConfig {
    // ...
}