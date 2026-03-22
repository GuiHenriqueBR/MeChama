package com.mechama.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PortfolioItemRequest {

    @NotBlank(message = "Título do trabalho é obrigatório")
    @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
    private String title;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    /** URL da imagem já hospedada (S3 ou outro storage) */
    private String photoUrl;
}
