package techblog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import techblog.domain.UserDetailsImpl;
import techblog.dto.request.ChangePasswordRequest;
import techblog.dto.request.UpdateProfileRequest;
import techblog.dto.response.BookmarkResponse;
import techblog.dto.response.ErrorResponse;
import techblog.dto.response.UserResponse;
import techblog.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "사용자 API")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 프로필 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    @Operation(summary = "사용자 프로필 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "중복된 닉네임")
    })
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    @Operation(summary = "북마크 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/bookmarks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BookmarkResponse>> getBookmarks(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getBookmarks(
                userDetails.getUsername(),
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        ));
    }

    @Operation(summary = "북마크 추가")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 추가 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 북마크된 게시글")
    })
    @PostMapping("/bookmarks/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookmarkResponse> addBookmark(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "게시글 ID") @PathVariable String postId) {
        return ResponseEntity.ok(userService.addBookmark(userDetails.getUsername(), postId));
    }

    @Operation(summary = "북마크 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "북마크 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    @DeleteMapping("/bookmarks/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeBookmark(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "게시글 ID") @PathVariable String postId) {
        userService.removeBookmark(userDetails.getUsername(), postId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "관심 태그 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관심 태그 수정 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PutMapping("/interests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateInterests(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @NotEmpty(message = "관심 태그는 최소 1개 이상이어야 합니다") List<String> interests) {
        return ResponseEntity.ok(userService.updateInterests(userDetails.getUsername(), interests));
    }

    @Operation(summary = "비밀번호 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 비밀번호"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }
}