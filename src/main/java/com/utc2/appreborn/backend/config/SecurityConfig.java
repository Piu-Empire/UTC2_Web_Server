package com.utc2.appreborn.backend.config;

import com.utc2.appreborn.backend.middleware.JwtFilter;
import com.utc2.appreborn.backend.security.jwt.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint entryPoint;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Mobile — public (không cần token)
                        .requestMatchers("/api/v1/notification/**").permitAll()
                        .requestMatchers("/api/v1/schedule/meta", "/api/v1/schedule/file").permitAll()
                        // Swagger / API docs
                        .requestMatchers("/v3/api-docs/**", "/api-docs/**",
                                         "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Admin schedule — yêu cầu token (anyRequest bên dưới cover)
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
