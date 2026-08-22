package com.technovisions.ordersystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalApiKeyFilterTest {

    private static final String HEADER = "X-Internal-Api-Key";
    private static final String EXPECTED_KEY = "test-key";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private InternalApiKeyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new InternalApiKeyFilter();
        ReflectionTestUtils.setField(filter, "expectedApiKey", EXPECTED_KEY);
    }

    @Test
    void doFilter_continuesChain_whenApiKeyIsValid() throws Exception {
        when(request.getRequestURI()).thenReturn("/internal/customers");
        when(request.getHeader(HEADER)).thenReturn(EXPECTED_KEY);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_rejectsWith401_whenApiKeyIsMissing() throws Exception {
        when(request.getRequestURI()).thenReturn("/internal/customers");
        when(request.getHeader(HEADER)).thenReturn(null);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_rejectsWith401_whenApiKeyIsWrong() throws Exception {
        when(request.getRequestURI()).thenReturn("/internal/orders");
        when(request.getHeader(HEADER)).thenReturn("wrong-key");
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_skipsCheck_whenPathIsNotInternal() throws Exception {
        when(request.getRequestURI()).thenReturn("/actuator/health");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
