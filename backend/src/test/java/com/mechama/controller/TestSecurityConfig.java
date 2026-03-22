package com.mechama.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança mínima para testes de controller (@WebMvcTest).
 *
 * Substitui o SecurityConfig real para remover a dependência do JwtAuthenticationFilter
 * e liberar todos os endpoints para os testes de unidade dos controllers.
 *
 * Não usar em testes de integração que precisem validar autenticação/autorização.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
