package com.mechama.dto;

import com.mechama.model.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail é obrigatório")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String password;

    /** Telefone opcional no cadastro, poderá ser solicitado em módulos futuros */
    private String phone;

    @NotNull(message = "Tipo de conta é obrigatório (CLIENT ou PROVIDER)")
    private UserType type;
}
