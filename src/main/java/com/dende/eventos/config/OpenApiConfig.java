package com.dende.eventos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dendê Eventos API")
                        .version("1.0.0")
                        .description("""
                                API REST para gerenciamento de eventos e inscrições.
                                
                                ## Funcionalidades
                                - Criação e gerenciamento de eventos
                                - Controle de status: ATIVO, CANCELADO, ENCERRADO, ESGOTADO
                                - Inscrição e cancelamento de participantes
                                - Validação de capacidade máxima
                                - Prevenção de duplicatas por e-mail e CPF
                                """)
                        .contact(new Contact()
                                .name("Time Manchester")
                                .email("manchester@dende.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Desenvolvimento"),
                        new Server().url("https://api.dende-eventos.com").description("Produção")
                ));
    }
}
