package techblog.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        Set<String> interestedTags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getInterestedTags(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}