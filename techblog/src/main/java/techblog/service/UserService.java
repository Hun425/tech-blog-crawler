package techblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import techblog.domain.BlogPost;
import techblog.domain.Bookmark;
import techblog.domain.User;
import techblog.dto.request.ChangePasswordRequest;
import techblog.dto.request.UpdateProfileRequest;
import techblog.dto.response.BookmarkResponse;
import techblog.dto.response.UserResponse;
import techblog.exception.BusinessException;
import techblog.exception.ErrorCode;
import techblog.repository.jpa.BlogPostJpaRepository;
import techblog.repository.jpa.BookmarkRepository;
import techblog.repository.jpa.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final BlogPostJpaRepository blogPostRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 프로필 조회
     */
    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        String.format("이메일 %s에 해당하는 사용자를 찾을 수 없습니다.", email)));
        return UserResponse.from(user);
    }

    /**
     * 사용자 프로필 업데이트
     */
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        String.format("이메일 %s에 해당하는 사용자를 찾을 수 없습니다.", email)));

        validateNickname(request.nickname(), user.getId());

        user.updateProfile(request.nickname());
        return UserResponse.from(user);
    }

    /**
     * 사용자 북마크 목록 조회
     */
    public Page<BookmarkResponse> getBookmarks(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        String.format("이메일 %s에 해당하는 사용자를 찾을 수 없습니다.", email)));

        return bookmarkRepository.findByUserAndDeletedFalse(user, pageable)
                .map(BookmarkResponse::from);
    }

    /**
     * 북마크 추가
     */
    @Transactional
    public BookmarkResponse addBookmark(String email, String postId) {
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        String.format("이메일 %s에 해당하는 사용자를 찾을 수 없습니다.", email)));

        // 게시글 조회
        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND,
                        String.format("게시글 ID %s를 찾을 수 없습니다.", postId)));

        // 중복 검사
        validateBookmarkDuplicate(user, post);

        // 북마크 생성 및 저장
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .post(post)
                .build();

        log.info("북마크 생성: 사용자 ID = {}, 게시글 ID = {}", user.getId(), post.getId());
        bookmarkRepository.save(bookmark);
        return BookmarkResponse.from(bookmark);
    }

    /**
     * 닉네임 중복 검사
     */
    private void validateNickname(String nickname, Long userId) {
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME,
                    String.format("닉네임 %s는 이미 사용중입니다.", nickname));
        }
    }

    /**
     * 북마크 중복 검사
     */
    private void validateBookmarkDuplicate(User user, BlogPost post) {
        if (bookmarkRepository.existsByUserAndPostAndDeletedFalse(user, post)) {
            throw new BusinessException(ErrorCode.DUPLICATE_BOOKMARK,
                    String.format("이미 북마크한 게시글입니다. 사용자 ID = %d, 게시글 ID = %s",
                            user.getId(), post.getId()));
        }
    }

    /**
     * 사용자 관심 태그 수정
     */
    @Transactional
    public UserResponse updateInterests(String email, List<String> interests) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        String.format("이메일 %s에 해당하는 사용자를 찾을 수 없습니다.", email)));

        user.updateInterests(new HashSet<>(interests));
        log.info("사용자 {} 관심 태그 수정: {}", email, interests);

        return UserResponse.from(user);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        String.format("이메일 %s에 해당하는 사용자를 찾을 수 없습니다.", email)));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 확인
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 변경
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        log.info("사용자 {} 비밀번호 변경 완료", email);
    }

    /**
     * 북마크 삭제
     */
    @Transactional
    public void removeBookmark(String email, String postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        String.format("이메일 %s에 해당하는 사용자를 찾을 수 없습니다.", email)));

        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND,
                        String.format("게시글 ID %s를 찾을 수 없습니다.", postId)));

        // 북마크 조회 및 삭제
        bookmarkRepository.findByUserAndPostAndDeletedFalse(user, post)
                .ifPresentOrElse(
                        bookmark -> {
                            bookmark.delete();
                            log.info("북마크 삭제: 사용자 ID = {}, 게시글 ID = {}", user.getId(), post.getId());
                        },
                        () -> {
                            throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND, "북마크를 찾을 수 없습니다.");
                        }
                );
    }
}