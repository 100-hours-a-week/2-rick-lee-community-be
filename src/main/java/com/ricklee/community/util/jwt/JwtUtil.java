package com.ricklee.community.util.jwt;

import com.ricklee.community.exception.custom.InvalidTokenException;
import com.ricklee.community.exception.custom.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JWT 토큰 생성, 검증 등의 기능을 제공하는 유틸리티 클래스
 */
@Component
public class JwtUtil {

    // JWT 시크릿 키
    @Value("${jwt.secret}")
    private String jwtSecret;

    // 토큰 만료 시간 (밀리초)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // JWT 토큰 접두사 (Bearer)
    private static final String TOKEN_PREFIX = "Bearer ";

    // 인증 헤더 명
    private static final String HEADER_STRING = "Authorization";

    // 사용자 역할 클레임
    private static final String AUTHORITIES_KEY = "role";

    // SecretKey 객체
    private SecretKey secretKey;

    /**
     * 초기화 메서드 - 시크릿 키 생성
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 토큰 생성
     *
     * @param userId 사용자 ID
     * @param role   사용자 역할
     * @return 생성된 JWT 토큰
     */
    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim(AUTHORITIES_KEY, role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     * @throws TokenExpiredException 토큰이 만료된 경우
     * @throws InvalidTokenException 토큰이 유효하지 않은 경우
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("토큰이 만료되었습니다.", e);
        } catch (SignatureException e) {
            throw new InvalidTokenException("토큰 서명이 유효하지 않습니다.", e);
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("잘못된 형식의 토큰입니다.", e);
        } catch (Exception e) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.", e);
        }
    }

    /**
     * 토큰에서 사용자 역할 추출
     *
     * @param token JWT 토큰
     * @return 사용자 역할
     * @throws TokenExpiredException 토큰이 만료된 경우
     * @throws InvalidTokenException 토큰이 유효하지 않은 경우
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get(AUTHORITIES_KEY, String.class);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("토큰이 만료되었습니다.", e);
        } catch (SignatureException e) {
            throw new InvalidTokenException("토큰 서명이 유효하지 않습니다.", e);
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("잘못된 형식의 토큰입니다.", e);
        } catch (Exception e) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.", e);
        }
    }

    /**
     * 요청 헤더에서 토큰 추출
     *
     * @param request HTTP 요청
     * @return 추출된 토큰 (없으면 null)
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_STRING);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 토큰으로부터 인증 정보 생성
     *
     * @param token JWT 토큰
     * @return Authentication 객체
     * @throws TokenExpiredException 토큰이 만료된 경우
     * @throws InvalidTokenException 토큰이 유효하지 않은 경우
     */
    public Authentication getAuthentication(String token) {
        Long userId = getUserIdFromToken(token);
        String role = getRoleFromToken(token);

        List<SimpleGrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }
}