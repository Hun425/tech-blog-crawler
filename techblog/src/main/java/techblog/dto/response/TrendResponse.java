package techblog.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record TrendResponse(
        List<KeywordTrend> keywords,
        Map<String, List<String>> companyTrends,
        LocalDateTime analyzedAt
) {
    public record KeywordTrend(
            String keyword,
            Long count,
            Double growthRate,
            List<String> relatedTags
    ) {}
}