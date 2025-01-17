package com.palgona.palgona.config;

import com.palgona.palgona.common.jwt.filter.JwtAuthenticationFilter;
import com.palgona.palgona.common.jwt.handler.JwtAccessDeniedHandler;
import com.palgona.palgona.common.jwt.handler.JwtAuthenticationEntryPoint;
import com.palgona.palgona.common.jwt.util.JwtUtils;
import com.palgona.palgona.common.redis.RedisUtils;
import com.palgona.palgona.member.domain.MemberRepository;
import com.palgona.palgona.common.jwt.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String USER = "USER";
    private static final String GUEST = "GUEST";

    private final JwtUtils jwtUtils;
    private final MemberRepository memberRepository;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final TokenExtractor tokenExtractor;
    private final RedisUtils redisUtils;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandlingConfigurer -> {
                    exceptionHandlingConfigurer.authenticationEntryPoint(jwtAuthenticationEntryPoint);
                    exceptionHandlingConfigurer.accessDeniedHandler(jwtAccessDeniedHandler);
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/tests/**").permitAll()
                        .requestMatchers("/api/v1/auth/signup").hasRole(GUEST)
                        .requestMatchers("api/v1/auth/logout", "api/v1/auth/refresh-token").hasRole(USER)
                        .requestMatchers("/v3/**", "swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/members").hasRole(ADMIN)
                        .requestMatchers("/api/v1/members/**").hasRole(USER)
                        .requestMatchers("/api/v1/products/**").hasRole(USER)
                        .requestMatchers("/api/v1/biddings/**").hasRole(USER)
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtils, memberRepository, tokenExtractor, redisUtils),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER > ROLE_GUEST");

        return hierarchy;
    }
}
