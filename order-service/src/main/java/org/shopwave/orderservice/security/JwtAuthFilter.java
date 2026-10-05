package org.shopwave.orderservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.UUID;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String token = jwtUtil.extractTokenFromRequest(request);

        if (token != null && jwtUtil.isTokenValid(token)) {
            try {
                UUID userId = jwtUtil.extractUserId(token);
                
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userId, 
                        null, 
                        java.util.Collections.emptyList()
                    );
                    
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT authenticated user: {}", userId);
                
            } catch (Exception e) {
                log.warn("Failed to authenticate JWT token: {}", e.getMessage());
                // Continue without authentication → will result in 401 for protected endpoints
            }
        }

        filterChain.doFilter(request, response);
    }
}