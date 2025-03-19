package com.ricklee.community.config.jwt.filter;

import com.ricklee.community.util.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * 모든 요청에 대해 JWT 토큰 검증 및 인증 정보 설정
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 요청에서 JWT 토큰 추출
            String jwt = jwtUtil.resolveToken(request);

            // 토큰이 유효하면 인증 정보 설정
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                Authentication authentication = jwtUtil.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("JWT 인증 처리 중 오류 발생", e);
            // 인증 실패 시에도 필터 체인은 계속 진행
            // 이후 필터나 컨트롤러에서 인증 여부에 따라 처리
        }

        // 다음 필터 실행
        filterChain.doFilter(request, response);
    }
}