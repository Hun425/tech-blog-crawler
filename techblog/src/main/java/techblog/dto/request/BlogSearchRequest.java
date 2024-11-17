package techblog.dto.request;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;


public record BlogSearchRequest(
        String keyword,
        List<String> companies,
        List<String> tags,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int size
) {
    // 정적 팩토리 메서드
    public static BlogSearchRequest of(String keyword, List<String> companies) {
        return new BlogSearchRequest(keyword, companies, null, null, null, 0, 20);
    }
}