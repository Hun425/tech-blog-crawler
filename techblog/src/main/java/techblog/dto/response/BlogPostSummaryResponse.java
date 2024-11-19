package techblog.dto.response;

import techblog.domain.BlogPost;

import java.time.LocalDateTime;

public record BlogPostSummaryResponse(
        Long id,
        String title,
        String company,
        LocalDateTime publishDate
) {
    public static BlogPostSummaryResponse from(BlogPost post) {
        return new BlogPostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCompany(),
                post.getPublishDate()
        );
    }
}
