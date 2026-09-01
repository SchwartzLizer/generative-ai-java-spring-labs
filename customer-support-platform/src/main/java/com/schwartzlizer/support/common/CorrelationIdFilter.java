package com.schwartzlizer.support.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Correlation-ID";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String value=request.getHeader(HEADER);
        if(value == null || value.isBlank() || value.length()>100) value=UUID.randomUUID().toString();
        response.setHeader(HEADER, value);
        MDC.put("correlationId", value);
        try {
            filterChain.doFilter(request,response);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
