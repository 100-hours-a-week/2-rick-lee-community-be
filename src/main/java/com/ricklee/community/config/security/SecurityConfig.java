package com.ricklee.community.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricklee.community.config.jwt.filter.JwtAuthenticationFilter;
import com.ricklee.community.util.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setAllowedMethods(Collections.singletonList("*"));
            config.setAllowedOriginPatterns(Arrays.asList(
                    "http://localhost:3000",
                    "http://127.0.0.1:3000",
                    "http://localhost:5500",
                    "http://127.0.0.1:5500"
            ));
            config.setAllowCredentials(true);
            return config;
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 토큰 방식을 위한 STATELESS 선언
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 권한 규칙 설정 (API 명세에 맞게 수정 필요)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                "/users/signup",
                                "/users/login"
                        )
                        .permitAll()  // 인증 없이 접근 가능한 URI 추가
                        .requestMatchers("/posts/**").authenticated()  // 게시글 관련 경로는 인증 필요
                        .requestMatchers("/users/**").hasRole("MEMBER")
                        .anyRequest().permitAll()  // 그 외 요청은 인가처리를 할 필요가 없음
                )
                // CORS 해결하기 위한 코드 추가
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                // 커스텀 JWT 핸들러 및 엔트리 포인트를 사용하기 위해 httpBasic disable
                .httpBasic(AbstractHttpConfigurer::disable)
                // 예외 처리 설정
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 인증 실패 (401 Unauthorized)
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");

                            Map<String, Object> errorResponse = new HashMap<>();
                            errorResponse.put("message", "unauthorized");
                            errorResponse.put("error", "인증에 실패했습니다. 유효한 토큰이 필요합니다.");
                            errorResponse.put("data", null);

                            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
                            response.getWriter().write(jsonResponse);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // 권한 부족 (403 Forbidden)
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");

                            Map<String, Object> errorResponse = new HashMap<>();
                            errorResponse.put("message", "access_denied");
                            errorResponse.put("error", "해당 리소스에 접근할 권한이 없습니다.");
                            errorResponse.put("data", null);

                            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
                            response.getWriter().write(jsonResponse);
                        })
                )
                // JWT Filter 를 필터체인에 끼워넣어줌
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    @Bean
    @Lazy
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}