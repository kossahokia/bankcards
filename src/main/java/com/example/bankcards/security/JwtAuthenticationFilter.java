package com.example.bankcards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security filter responsible for processing and validating JWT tokens
 * for each incoming HTTP request.
 * <p>
 * This filter intercepts requests before they reach controllers, extracts
 * the JWT from the {@code Authorization} header, validates it using
 * {@link JwtTokenProvider}, and if valid — sets the corresponding user
 * authentication in the {@link SecurityContextHolder}.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Extract JWT tokens from request headers.</li>
 *   <li>Validate tokens via {@link JwtTokenProvider}.</li>
 *   <li>Load user details using {@link UserDetailsServiceImpl}.</li>
 *   <li>Populate Spring Security's context with the authenticated user.</li>
 * </ul>
 *
 * <h3>Example Authorization header:</h3>
 * <pre>{@code
 * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 * }</pre>
 *
 * <p>
 * This filter extends {@link OncePerRequestFilter}, ensuring it runs
 * only once per request within a single request-processing cycle.
 * </p>
 *
 * @see JwtTokenProvider
 * @see UserDetailsServiceImpl
 * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
 * @see org.springframework.security.core.context.SecurityContextHolder
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Filters each incoming request, checking for a valid JWT token.
     * If valid, sets the authenticated user in the security context.
     *
     * @param request the current HTTP request
     * @param response the current HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Failed to authenticate user via JWT", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts a JWT token from the Authorization header if present.
     * Expected format: {@code Bearer <token>}.
     *
     * @param request the current HTTP request
     * @return the JWT token string, or {@code null} if not found
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
