package techblog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawling")
public class CrawlingController {
    private final CrawlingService crawlingService;

    @PostMapping("/start")
    public ResponseEntity<String> startCrawling() {
        try {
            crawlingService.crawlAllBlogs();
            return ResponseEntity.ok("크롤링이 성공적으로 시작되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("크롤링 실행 중 오류 발생: " + e.getMessage());
        }
    }
}
