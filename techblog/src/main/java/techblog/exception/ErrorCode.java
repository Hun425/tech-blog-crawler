package techblog.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 공통 에러
    INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값"),
    INTERNAL_SERVER_ERROR(500, "C002", "서버 내부 에러"),
    ENTITY_NOT_FOUND(400, "C003", "엔티티를 찾을 수 없음"),

    // 인증 관련 에러
    INVALID_TOKEN(401, "A001", "유효하지 않은 토큰"),
    EXPIRED_TOKEN(401, "A002", "만료된 토큰"),
    UNAUTHORIZED(401, "A003", "인증되지 않은 접근"),
    INVALID_REFRESH_TOKEN(401,"A004","유효하지 않은 리프레시 토큰"),

    // 사용자 관련 에러
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없음"),
    DUPLICATE_EMAIL(409, "U002", "이미 존재하는 이메일"),
    DUPLICATE_NICKNAME(409, "U003", "이미 존재하는 닉네임"),
    INVALID_PASSWORD(400, "U004", "잘못된 비밀번호"),
    PASSWORD_MISMATCH(400, "U005", "비밀번호 불일치"),

    // 블로그 포스트 관련 에러
    POST_NOT_FOUND(404, "P001", "게시글을 찾을 수 없음"),

    // 북마크 관련 에러
    DUPLICATE_BOOKMARK(409, "B001", "이미 북마크된 게시글"),
    BOOKMARK_NOT_FOUND(404, "B002", "북마크를 찾을 수 없음");

    private final int status;    // HTTP 상태 코드
    private final String code;   // 내부 에러 코드
    private final String message;// 에러 메시지
}