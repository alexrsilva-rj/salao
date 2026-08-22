package com.salao.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

   private static final String API_TOKEN_HEADER = "x-api-token";
   private static final String VALID_API_TOKEN = "salao-secret-api-token-123"; // Exemplo (em prod, consulte cache/banco)

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
           throws ServletException, IOException {

       String apiToken = request.getHeader(API_TOKEN_HEADER);

       if (apiToken != null && !apiToken.isBlank()) {
           if (VALID_API_TOKEN.equals(apiToken)) {
               List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                       new SimpleGrantedAuthority("SCOPE_agendamento:escrever")
               );

               UsernamePasswordAuthenticationToken authentication =
                       new UsernamePasswordAuthenticationToken("ApiTokenUser", null, authorities);

               SecurityContextHolder.getContext().setAuthentication(authentication);
           } else {
               response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token de API inválido.");
               return;
           }
       }

       filterChain.doFilter(request, response);
   }
}
