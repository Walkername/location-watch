package ru.locationwatch.backend.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.locationwatch.backend.services.PersonDetailsService;
import ru.locationwatch.backend.services.TokenService;
import ru.locationwatch.backend.util.ErrorResponse;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final PersonDetailsService personDetailsService;

    @Autowired
    public JWTFilter(
            TokenService tokenService,
            PersonDetailsService personDetailsService
    ) {
        this.tokenService = tokenService;
        this.personDetailsService = personDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.length() == 6) {
            authHeader = authHeader + " ";
        }

        if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (token.isBlank()) {
                setResponse(response, request, "JWT token was not found");
                return;
            } else {
                try {
                    DecodedJWT jwt = tokenService.validateAccessToken(token);
                    String username = jwt.getClaim("username").asString();
                    UserDetails userDetails = personDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            userDetails.getPassword(),
                            userDetails.getAuthorities()
                    );

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (JWTVerificationException e) {
                    setResponse(response, request, "Invalid JWT token");
                    return;
                }

            }
        }

        filterChain.doFilter(request, response);
    }

    private void setResponse(HttpServletResponse response, HttpServletRequest request, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(
                message, System.currentTimeMillis()
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
        response.getWriter().close();
    }

}
