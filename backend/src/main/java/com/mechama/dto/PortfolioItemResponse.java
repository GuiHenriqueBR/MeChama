package com.mechama.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PortfolioItemResponse {
    private Long id;
    private String title;
    private String description;
    private String photoUrl;
    private LocalDateTime createdAt;
}
