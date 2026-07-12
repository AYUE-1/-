package com.petadoption.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 只对 /api/** 路径应用安全规则，其余全部放行
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pets/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
                .requestMatchers("/api/articles/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/community/posts/**").permitAll()
                .requestMatchers("/api/community/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/rescue/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/assessment/questions/**").permitAll()
                .requestMatchers("/api/assessment/submit").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/assessment/result/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/welfare/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/trust/reviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/community/comments/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shelter/profile/**").permitAll()
                // API 文档
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/doc.html").permitAll()
                // 管理接口仅 ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 其余 API 需认证
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
