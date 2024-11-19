package techblog.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import techblog.domain.User;
import techblog.exception.BusinessException;
import techblog.exception.ErrorCode;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final RedisTemplate<String, String> redisTemplate;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(User user) {
        return buildToken(new HashMap<>(), user, accessTokenExpiration);
    }

    public String generateRefreshToken(User user) {
        String refreshToken = buildToken(new HashMap<>(), user, refreshTokenExpiration);
        // Redis에 리프레시 토큰 저장
        saveRefreshTokenToRedis(user.getEmail(), refreshToken);
        return refreshToken;
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            User user,
            long expiration
    ) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public String validateRefreshToken(String refreshToken) {
        // 토큰 유효성 검사
        if (isTokenExpired(refreshToken)) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        // 사용자 이메일 추출
        String userEmail = extractUsername(refreshToken);

        // Redis에서 저장된 리프레시 토큰 조회
        String savedToken = getRefreshTokenFromRedis(userEmail);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return userEmail;
    }

    private void saveRefreshTokenToRedis(String userEmail, String refreshToken) {
        // Redis에 리프레시 토큰 저장 (만료 시간 설정)
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(
                "RT:" + userEmail,
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );
    }

    private String getRefreshTokenFromRedis(String userEmail) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return values.get("RT:" + userEmail);
    }

    public void deleteRefreshToken(String userEmail) {
        redisTemplate.delete("RT:" + userEmail);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}