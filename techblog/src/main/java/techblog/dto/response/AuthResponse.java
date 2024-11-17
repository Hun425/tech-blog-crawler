package techblog.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record AuthResponse(
        String token,
        String email,
        String nickname,
        Set<String> interestedTags,
        LocalDateTime createdAt
) {
    public static AuthResponse from(User user, String token) {
        return new AuthResponse(
                token,
                user.getEmail(),
                user.getNickname(),
                user.getInterestedTags(),
                user.getCreatedAt()
        );
    }
}