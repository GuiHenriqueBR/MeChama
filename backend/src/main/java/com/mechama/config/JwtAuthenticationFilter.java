package com.mechama.config;

import com.mechama.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Intercepta toda requisição, extrai e valida o Bearer token JWT.
 *
 * Fluxo:
 * 1. Lê o header Authorization: Bearer <token>
 * 2. Valida o token com JwtService
 * 3. Injeta no SecurityContext: userId (como principal), role (como authority)
 *
 * Controllers e Services podem então chamar:
 *   SecurityContextHolder.getContext().getAuthentication().getName()
 * para obter o userId do usuário autenticado.
 *
 * Integração futura:
 * - Quando implementarmos refresh token, este filtro será estendido
 *   para lidar com tokens expirados e chamar a rota de refresh.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtService.extractUserId(token);
        String userType = jwtService.extractClaims(token).get("type", String.class);

        // Monta a autenticação com o role do usuário (ex.: ROLE_PROVIDER, ROLE_CLIENT)
        var authority = new SimpleGrantedAuthority("ROLE_" + userType);
        var authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(),
                null,
                List.of(authority)
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
