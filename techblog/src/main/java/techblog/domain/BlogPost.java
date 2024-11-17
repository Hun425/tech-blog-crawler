package techblog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<String> tags;

    private String url;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}