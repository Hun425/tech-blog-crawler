package techblog.dto.response;

import techblog.domain.BlogPost;

import java.time.LocalDateTime;
import java.util.List;

public record BlogPostResponse(
        String id,
        String title,
        String content,
        String company,
        LocalDateTime publishDate,
        List<String> tags,
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