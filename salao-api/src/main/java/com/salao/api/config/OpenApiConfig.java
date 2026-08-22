package com.salao.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

   @Bean
   public OpenAPI customOpenAPI() {
       final String securitySchemeName = "keycloakOAuth";
       
       return new OpenAPI()
               .info(new Info()
                       .title("API Salão de Beleza")
                       .version("1.0.0")
                       .description("Documentação da API com integração OAuth2 / Keycloak e validação de escopos."))
               .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
               .components(new Components()
                       .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                               .type(SecurityScheme.Type.OAUTH2)
                               .description("Autenticação OAuth2 via Keycloak")
                               .flows(new OAuthFlows()
                                       .authorizationCode(new OAuthFlow()
                                               .authorizationUrl("http://192.168.18.200:8080/realms/salao-realm/protocol/openid-connect/auth")
                                               .tokenUrl("http://192.168.18.200:8080/realms/salao-realm/protocol/openid-connect/token")
                                               .scopes(new Scopes()
                                                       .addString("agendamento:escrever", "Permissão para criar agendamentos")
                                               )
                                       )
                               )
                       )
               );
   }
}
