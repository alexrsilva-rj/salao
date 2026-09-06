package com.salao.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiTokenAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateValidApiToken() throws ServletException, IOException {
        ApiTokenAuthenticationFilter filter = new ApiTokenAuthenticationFilter("segredo-forte-123");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-api-token", "segredo-forte-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("ApiTokenUser", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPTION")));
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldRejectInvalidApiToken() throws ServletException, IOException {
        ApiTokenAuthenticationFilter filter = new ApiTokenAuthenticationFilter("segredo-forte-123");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-api-token", "token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void shouldRejectWhenServerSecretIsEmpty() throws ServletException, IOException {
        ApiTokenAuthenticationFilter filter = new ApiTokenAuthenticationFilter("");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-api-token", "qualquer-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void shouldPassThroughWhenNoHeaderPresent() throws ServletException, IOException {
        ApiTokenAuthenticationFilter filter = new ApiTokenAuthenticationFilter("segredo-forte-123");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, times(1)).doFilter(request, response);
    }
}
