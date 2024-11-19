package techblog.domain.vo;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class UserProfileVO {
    private final String nickname;
    private final Set<String> interests;

    public UserProfileVO(String nickname, Set<String> interests) {
        this.nickname = nickname;
        this.interests = new HashSet<>(interests);
    }
}