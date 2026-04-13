package com.attendance.security;

import com.attendance.service.JwtService;
import com.attendance.repository.TokenBlacklistRepository;
import com.attendance.repository.UserRepository;
import com.attendance.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7).trim();
        try {
            Claims claims = jwtService.parseClaims(jwt);
            String tokenType = claims.get("token_type", String.class);
            
            // Chỉ chấp nhận ACCESS token cho các request thông thường
            if (!"ACCESS".equals(tokenType)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Kiểm tra blacklist
            String jti = claims.getId();
            if (tokenBlacklistRepository.existsByTokenJti(jti)) {
                filterChain.doFilter(request, response);
                return;
            }

            String userSubject = claims.getSubject();

            if (userSubject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Ưu tiên lấy roles trực tiếp từ JWT claims
                List<?> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities;
                
                if (roles != null) {
                    authorities = roles.stream()
                            .map(r -> new SimpleGrantedAuthority(r.toString()))
                            .collect(Collectors.toList());
                } else {
                    // Fallback load user từ database (EAGER roles)
                    User user = resolveUserFromClaims(claims);
                    if (user == null || !user.isEnabled()) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    authorities = user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                            .collect(Collectors.toList());
                }

                // Tạo Authentication object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userSubject, // Principal có thể là ID (subject) hoặc User object
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Token không hợp lệ
        }

        filterChain.doFilter(request, response);
    }

    private User resolveUserFromClaims(Claims claims) {
        String subject = claims.getSubject();
        if (subject == null) return null;

        boolean numericSubject = subject.chars().allMatch(Character::isDigit);
        if (numericSubject) {
            Long userId = Long.parseLong(subject);
            return userRepository.findById(userId).orElse(null);
        }
        return userRepository.findByUsername(subject).orElse(null);
    }
}




