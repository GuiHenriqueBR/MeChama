package com.mechama.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração de segurança do MeChama.
 *
 * Tarefa 2: JwtAuthenticationFilter adicionado ao chain.
 * Endpoints protegidos agora exigem Bearer token válido.
 *
 * @EnableMethodSecurity habilita @PreAuthorize nos controllers
 * para checagens de papel (ex.: apenas PROVIDER acessa seu dashboard).
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Autenticação pública
                .requestMatchers("/api/auth/**").permitAll()
                // Perfil público de prestador — leitura sem autenticação
                .requestMatchers("GET", "/api/providers/*/profile").permitAll()
                .requestMatchers("GET", "/api/providers/*/portfolio").permitAll()
                // Catálogo de serviços — leitura pública (T3)
                .requestMatchers("GET", "/api/categories").permitAll()
                .requestMatchers("GET", "/api/providers/*/services").permitAll()
                .requestMatchers("GET", "/api/services/*").permitAll()
                // Todo o resto exige JWT
                .anyRequest().authenticated()
            )
            // Injeta o filtro JWT antes do filtro padrão de usuário/senha
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS permissivo para desenvolvimento.
     * Em produção: restringir origins para o domínio real do app.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
