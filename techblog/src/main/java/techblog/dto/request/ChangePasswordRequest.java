package techblog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "현재 닉네임은 필수입니다.")
        String currentPassword,
        @NotBlank(message = "새로운 패스워드는 필수입니다.")
        @Size(min = 8, message = "최소 8글자 이상입니다.")
        String newPassword,


        @NotBlank(message = "Password confirmation is required")
        String confirmPassword
) {}