package techblog.domain;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Document(indexName = "blog-posts")
@NoArgsConstructor
public class BlogPostDocument {
    @org.springframework.data.annotation.Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;

    @Field(type = FieldType.Keyword)
    private String company;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime publishDate;

    @Field(type = FieldType.Keyword)
    private Set<String> tags;

    @Field(type = FieldType.Keyword)
    private String url;

    @Builder
    public BlogPostDocument(String id, String title, String content, String company,
                            String url, LocalDateTime publishDate, Set<String> tags) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.company = company;
        this.url = url;
        this.publishDate = publishDate;
        this.tags = tags;
    }

    public static BlogPostDocument from(BlogPost post) {
        return BlogPostDocument.builder()
                .id(post.getUrl()) // URL을 id로 사용
                .title(post.getTitle())
                .content(post.getContent())
                .company(post.getCompany())
                .url(post.getUrl())
                .publishDate(post.getPublishDate())
                .tags(post.getTags())
                .build();
    }
}