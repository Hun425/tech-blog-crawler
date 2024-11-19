package techblog.dto.response;

import org.springframework.validation.BindingResult;
import techblog.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record ErrorResponse(
        LocalDateTime timestamp,    // 에러 발생 시간
        String code,               // 에러 코드 (예: "U001")
        int status,                // HTTP 상태 코드
        String message,            // 에러 메시지
        String path,               // 요청 경로
        List<FieldError> errors    // 필드 에러 목록
) {
    private static final String DEFAULT_ERROR_MESSAGE = "알 수 없는 에러가 발생했습니다.";

    /**
     * ErrorCode로 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getCode(),
                errorCode.getStatus(),
                errorCode.getMessage(),
                path,
                Collections.emptyList()
        );
    }

    /**
     * ErrorCode와 커스텀 메시지로 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getCode(),
                errorCode.getStatus(),
                message,
                path,
                Collections.emptyList()
        );
    }

    /**
     * 폼 유효성 검사 실패 시 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getCode(),
                errorCode.getStatus(),
                errorCode.getMessage(),
                path,
                FieldError.of(bindingResult)
        );
    }

    /**
     * 필드 에러 정보를 담는 레코드
     */
    public record FieldError(
            String field,           // 에러가 발생한 필드명
            String value,           // 에러가 발생한 값
            String reason           // 에러 발생 이유
    ) {
        public static List<FieldError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()
                    ))
                    .collect(Collectors.toList());
        }
    }
}