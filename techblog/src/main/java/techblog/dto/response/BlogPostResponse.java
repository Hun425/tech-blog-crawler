package techblog.dto.response;

import techblog.domain.BlogPost;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record BlogPostResponse(
        Long id,
        String title,
        String content,
        String company,
        LocalDateTime publishDate,
        Set<String> tags,
        String url,
        Long viewCount,
        boolean isBookmarked
) {
    public static BlogPostResponse from(BlogPost blogPost) {
        return new BlogPostResponse(
                blogPost.getId(),
                blogPost.getTitle(),
                blogPost.getContent(),
                blogPost.getCompany(),
                blogPost.getPublishDate(),
                blogPost.getTags(),
                blogPost.getUrl(),
                blogPost.getViewCount(),
                false
        );
    }

    public static BlogPostResponse from(BlogPost blogPost, boolean isBookmarked) {
        return new BlogPostResponse(
                blogPost.getId(),
                blogPost.getTitle(),
                blogPost.getContent(),
                blogPost.getCompany(),
                blogPost.getPublishDate(),
                blogPost.getTags(),
                blogPost.getUrl(),
                blogPost.getViewCount(),
                isBookmarked
        );
    }
}