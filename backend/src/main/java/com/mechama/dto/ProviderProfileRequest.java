package com.mechama.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Payload para criar ou atualizar o perfil do prestador.
 * Todos os campos são opcionais no update (PATCH semântico).
 */
@Data
public class ProviderProfileRequest {

    @Size(max = 1000, message = "Bio deve ter no máximo 1000 caracteres")
    private String bio;

    private String avatarUrl;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String neighborhood;

    @Min(value = 0, message = "Anos de experiência não pode ser negativo")
    @Max(value = 70, message = "Valor de experiência inválido")
    private Integer experienceYears;

    @Size(max = 10, message = "Máximo de 10 especialidades")
    private List<String> specialties;

    @Min(value = 1, message = "Raio mínimo de 1 km")
    @Max(value = 500, message = "Raio máximo de 500 km")
    private Integer serviceRadiusKm;

    private Boolean available;
}
