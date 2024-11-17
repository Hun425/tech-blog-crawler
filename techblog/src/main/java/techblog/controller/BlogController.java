package techblog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import techblog.dto.response.BlogPostResponse;
import techblog.service.BlogService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Blog Posts", description = "Blog Posts API")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @Operation(summary = "Get recent blog posts")
    @GetMapping("/recent")
    public ResponseEntity<List<BlogPostResponse>> getRecentPosts(
            @Parameter(description = "Number of posts to return")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogService.getRecentPosts(size));
    }

    @Operation(summary = "Search blog posts")
    @GetMapping("/search")
    public ResponseEntity<Page<BlogPostResponse>> searchPosts(
            @Parameter(description = "Search keyword")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Company names to filter")
            @RequestParam(required = false) List<String> companies,
            @Parameter(description = "Page number")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(blogService.searchPosts(keyword, companies,
                PageRequest.of(page, size)));
    }

    @Operation(summary = "Get trending keywords")
    @GetMapping("/trends/keywords")
    public ResponseEntity<List<TrendKeyword>> getTrendingKeywords() {
        return ResponseEntity.ok(blogService.getTrendingKeywords());
    }
}