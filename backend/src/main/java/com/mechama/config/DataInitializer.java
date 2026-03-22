package com.mechama.config;

import com.mechama.model.ServiceCategory;
import com.mechama.repository.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Popula as categorias de serviço na primeira execução.
 * Executa somente se a tabela service_categories estiver vazia (idempotente).
 *
 * Preços mínimos iniciais são simbólicos (R$ 30-80).
 * A IA da plataforma ajustará esses valores dinamicamente
 * com base no histórico de preços cadastrados por categoria (fase futura).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ServiceCategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return; // já populado
        }

        log.info("Populando categorias de serviço padrão...");

        List<ServiceCategory> categories = List.of(
            build("Elétrica",            "eletrica",            "⚡", new BigDecimal("80.00")),
            build("Hidráulica",          "hidraulica",          "🔧", new BigDecimal("80.00")),
            build("Limpeza",             "limpeza",             "🧹", new BigDecimal("60.00")),
            build("Pintura",             "pintura",             "🖌️", new BigDecimal("70.00")),
            build("Marcenaria",          "marcenaria",          "🪵", new BigDecimal("90.00")),
            build("Ar-condicionado",     "ar-condicionado",     "❄️", new BigDecimal("100.00")),
            build("Beleza e Estética",   "beleza",              "💇", new BigDecimal("50.00")),
            build("TI e Informática",    "ti-informatica",      "💻", new BigDecimal("80.00")),
            build("Reforma e Construção","reforma-construcao",  "🏗️", new BigDecimal("100.00")),
            build("Segurança",           "seguranca",           "🔒", new BigDecimal("80.00")),
            build("Jardinagem",          "jardinagem",          "🌿", new BigDecimal("50.00")),
            build("Mudança e Frete",     "mudanca-frete",       "🚛", new BigDecimal("120.00")),
            build("Aulas e Tutoria",     "aulas-tutoria",       "📚", new BigDecimal("50.00")),
            build("Fotografia e Vídeo",  "fotografia-video",    "📷", new BigDecimal("150.00")),
            build("Design e Criação",    "design-criacao",      "🎨", new BigDecimal("80.00")),
            build("Saúde e Bem-estar",   "saude-bem-estar",     "💊", new BigDecimal("60.00")),
            build("Pets",                "pets",                "🐾", new BigDecimal("50.00")),
            build("Eventos",             "eventos",             "🎉", new BigDecimal("150.00")),
            build("Outros",              "outros",              "🛠️", new BigDecimal("30.00"))
        );

        categoryRepository.saveAll(categories);
        log.info("{} categorias criadas com sucesso.", categories.size());
    }

    private ServiceCategory build(String name, String slug, String icon, BigDecimal minPrice) {
        return ServiceCategory.builder()
                .name(name)
                .slug(slug)
                .icon(icon)
                .minPrice(minPrice)
                .active(true)
                .build();
    }
}
