package techblog.dto.response;

import techblog.domain.Bookmark;

import java.time.LocalDateTime;

public record BookmarkResponse(
        Long id,
        BlogPostResponse post,
        LocalDateTime createdAt
) {
    public static BookmarkResponse from(Bookmark bookmark) {
        return new BookmarkResponse(
                bookmark.getId(),
                BlogPostResponse.from(bookmark.getPost()),
                bookmark.getCreatedAt()
        );
    }
}