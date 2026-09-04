package com.salao.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

   /**
    * URL do issuer do Keycloak — injetada via variável de ambiente.
    * Issue 08: elimina IP de LAN hardcoded (http://192.168.18.200:8080).
    */
   @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
   private String keycloakIssuerUri;

   @Bean
   public OpenAPI customOpenAPI() {
       final String securitySchemeName = "keycloakOAuth";

       String authUrl = keycloakIssuerUri + "/protocol/openid-connect/auth";
       String tokenUrl = keycloakIssuerUri + "/protocol/openid-connect/token";

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
                                               .authorizationUrl(authUrl)
                                               .tokenUrl(tokenUrl)
                                               .scopes(new Scopes()
                                                       .addString("agendamento:escrever", "Permissão para criar agendamentos")
                                               )
                                       )
                               )
                       )
               );
   }
}
