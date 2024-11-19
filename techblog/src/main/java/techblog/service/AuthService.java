package techblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import techblog.domain.RefreshToken;
import techblog.domain.Role;
import techblog.domain.User;
import techblog.dto.request.RefreshTokenRequest;
import techblog.dto.request.SignInRequest;
import techblog.dto.request.SignUpRequest;
import techblog.dto.response.AuthResponse;
import techblog.exception.BusinessException;
import techblog.exception.ErrorCode;
import techblog.repository.jpa.RefreshTokenRepository;
import techblog.repository.jpa.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    private final RedisTemplate<String, String> redisTemplate;  // RedisTemplate 추가

    public AuthResponse signUp(SignUpRequest request) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL,
                    String.format("이메일 %s는 이미 사용중입니다.", request.email()));
        }

        // 사용자 생성
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);
        log.info("회원가입 완료: {}", request.email());

        // 토큰 생성
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveRefreshToken(user, refreshToken);

        return AuthResponse.of(user, accessToken, refreshToken);
    }

    public AuthResponse signIn(SignInRequest request) {
        try {
            // 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 마지막 로그인 시간 업데이트
            user.updateLastLoginAt();

            // 토큰 생성
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            saveRefreshToken(user, refreshToken);

            log.info("로그인 성공: {}", request.email());
            return AuthResponse.of(user, accessToken, refreshToken);

        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "이메일 또는 비밀번호가 잘못되었습니다.");
        }
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        // 리프레시 토큰 검증
        String userEmail = jwtService.validateRefreshToken(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 새 토큰 발급
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // 리프레시 토큰 교체
        refreshToken.updateToken(newRefreshToken);

        log.info("토큰 재발급 완료: {}", userEmail);
        return AuthResponse.of(user, newAccessToken, newRefreshToken);
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .map(rt -> rt.updateToken(token))
                .orElse(RefreshToken.builder()
                        .user(user)
                        .token(token)
                        .build());

        refreshTokenRepository.save(refreshToken);
    }
}
