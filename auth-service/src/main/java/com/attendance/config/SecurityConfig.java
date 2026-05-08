package com.attendance.config;

import com.attendance.security.JwtAuthenticationEntryPoint;
import com.attendance.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/attendance/scan-by-face").permitAll()
                        .requestMatchers("/internal/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Role boundaries at the filter chain level. Method security below remains the final guard.

                        // Employee/Department/Position: GET → ADMIN, HR, MANAGER; CUD → ADMIN, HR
                        .requestMatchers(HttpMethod.GET, "/api/employees/**", "/api/departments/**", "/api/positions/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR", "ROLE_MANAGER")
                        .requestMatchers("/api/employees/**", "/api/departments/**", "/api/positions/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR")

                        // Shifts: ADMIN, HR full access
                        .requestMatchers("/api/attendance/shifts/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR")

                        // Holidays: GET → ALL roles; CUD → ADMIN, HR
                        .requestMatchers(HttpMethod.GET, "/api/attendance/holidays/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR", "ROLE_MANAGER", "ROLE_EMPLOYEE")
                        .requestMatchers("/api/attendance/holidays/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR")

                        // Attendance delete/check-in/check-out: ADMIN, HR
                        .requestMatchers(HttpMethod.DELETE, "/api/attendance/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR")
                        .requestMatchers("/api/attendance/check-in/**", "/api/attendance/check-out/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR")

                        // Schedule management: ADMIN, HR
                        .requestMatchers("/api/attendance/schedules/bulk", "/api/attendance/schedules/apply-template")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR")
                        .requestMatchers(HttpMethod.POST, "/api/attendance/schedules/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR")
                        .requestMatchers(HttpMethod.PUT, "/api/attendance/schedules/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/attendance/schedules/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR")

                        // Self-service endpoints: ALL roles
                        .requestMatchers("/api/leaves/me/**", "/api/leaves/types",
                                "/api/attendance/me", "/api/attendance/today/me",
                                "/api/attendance/schedules/me",
                                "/api/overtime-requests/me/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR", "ROLE_MANAGER", "ROLE_EMPLOYEE")

                        // Management endpoints: ADMIN, HR, MANAGER
                        .requestMatchers("/api/leaves/**", "/api/reports/**",
                                "/api/attendance/**", "/api/overtime-requests/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_HR", "ROLE_MANAGER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
