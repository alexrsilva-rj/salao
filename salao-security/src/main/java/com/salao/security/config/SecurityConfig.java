package com.salao.security.config;

import com.salao.security.filter.ApiTokenAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       http
           .csrf(csrf -> csrf.disable())
           .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
           // Insere o filtro do x-api-token antes do filtro de autenticação padrão
           .addFilterBefore(new ApiTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
           .authorizeHttpRequests(auth -> auth
               .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
               .anyRequest().authenticated()
           )
           .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

       return http.build();
   }
}
