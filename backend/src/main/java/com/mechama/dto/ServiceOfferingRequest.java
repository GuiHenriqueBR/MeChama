package com.mechama.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Payload para criar ou atualizar um serviço ofertado.
 *
 * A validação de preço mínimo por categoria é feita no ServiceOfferingService,
 * pois depende de consulta ao banco (não é validável somente com annotations).
 */
@Data
public class ServiceOfferingRequest {

    @NotNull(message = "Categoria é obrigatória")
    private Long categoryId;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
    private String title;

    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    private String description;

    @NotNull(message = "Preço base é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Formato de preço inválido")
    private BigDecimal basePrice;

    @Min(value = 15, message = "Duração mínima de 15 minutos")
    @Max(value = 14400, message = "Duração máxima de 240 horas")
    private Integer durationMinutes;

    @Size(max = 1000, message = "Campo 'incluso' deve ter no máximo 1000 caracteres")
    private String whatIsIncluded;

    @Size(max = 1000, message = "Campo 'não incluso' deve ter no máximo 1000 caracteres")
    private String whatIsNotIncluded;

    /** Permite ativar/desativar um serviço sem excluí-lo */
    private Boolean active;
}
