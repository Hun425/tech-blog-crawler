package techblog.domain;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Document(indexName = "blog-posts")
@NoArgsConstructor
public class BlogPostDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
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
    public BlogPostDocument(Long id,String title, String content, String company, String url,
                    LocalDateTime publishDate, Set<String> tags) {
        this.id = String.valueOf(id);
        this.title = title;
        this.content = content;
        this.company = company;
        this.url = url;
        this.publishDate = publishDate;
        this.tags = tags;
    }

    public static BlogPostDocument from(BlogPost post) {
     return BlogPostDocument.builder()
             .id(post.getId())
             .title(post.getTitle())
             .company(post.getCompany())
             .url(post.getUrl())
             .content(post.getContent())
             .tags(post.getTags())
             .publishDate(post.getPublishDate())
             .build();
    }

}
