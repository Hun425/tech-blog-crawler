package techblog.dto.response;

import techblog.domain.User;

import java.time.LocalDateTime;
import java.util.Set;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String nickname,
        Set<String> interestedTags,
        LocalDateTime createdAt
) {
    public static AuthResponse from(User user, String accessToken,String refreshToken) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getNickname(),
                user.getInterestedTags(),
                user.getCreatedAt()
        );
    }

    public static AuthResponse of(User user, String accessToken, String refreshToken) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getNickname(),
                user.getInterestedTags(),
                user.getCreatedAt()
        );
    }
}