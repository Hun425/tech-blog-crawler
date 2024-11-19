package techblog.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Document(indexName = "blog-posts")
@Entity
@Table(name = "blog_posts")
public class BlogPost {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    @Column(columnDefinition = "TEXT")
    private String content;

    @Field(type = FieldType.Keyword)
    private String company;

    @Field(type = FieldType.Date)
    private LocalDateTime publishDate;

    @Field(type = FieldType.Keyword)
    @ElementCollection
    private Set<String> tags;

    private String url;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public BlogPost(String title, String content, String company, String url,
                    LocalDateTime publishDate, Set<String> tags) {
        this.title = title;
        this.content = content;
        this.company = company;
        this.url = url;
        this.publishDate = publishDate;
        this.tags = tags;
    }
}