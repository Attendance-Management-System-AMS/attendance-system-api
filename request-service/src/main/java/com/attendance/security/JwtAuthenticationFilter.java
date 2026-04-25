package com.attendance.security;

import com.attendance.client.AuthClient;
import com.attendance.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthClient authClient;

    public JwtAuthenticationFilter(JwtService jwtService, AuthClient authClient) {
        this.jwtService = jwtService;
        this.authClient = authClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7).trim();
            Claims claims = jwtService.parseClaims(token);
            if (!"ACCESS".equals(claims.get("token_type", String.class))) {
                filterChain.doFilter(request, response);
                return;
            }
            String jti = claims.getId();
            if (jti != null && !jti.isBlank() && isBlacklisted(jti)) {
                filterChain.doFilter(request, response);
                return;
            }

            String subject = claims.getSubject();
            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        subject,
                        null,
                        authorities(claims.get("roles", Collection.class)));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Invalid tokens are treated as anonymous requests and rejected by authorization rules.
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> authorities(Collection<?> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(String::valueOf)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private boolean isBlacklisted(String jti) {
        try {
            return authClient.isTokenBlacklisted(jti);
        } catch (Exception ignored) {
            return true;
        }
    }
}
