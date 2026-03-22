package com.mechama.dto;

import com.mechama.model.UserType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Resposta padronizada de autenticação.
 * O campo `token` será usado em todos os módulos seguintes para
 * autenticar chamadas autenticadas (perfil, serviços, pedidos, pagamentos).
 */
@Data
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserType type;
    private boolean active;
    private LocalDateTime createdAt;

    /** JWT retornado no login e cadastro — guardar no AsyncStorage (mobile) */
    private String token;
}
