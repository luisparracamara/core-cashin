package com.core.cashin.routing.filter;

import com.core.cashin.commons.constants.HttpHeaders;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = HttpHeaders.X_CORRELATION_ID;
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            if (httpRequest.getRequestURI().contains("/oauth/callback")) {
                correlationId = java.util.UUID.randomUUID().toString();
                log.debug("[CorrelationId] auto-generated correlationId={} path={}", correlationId, httpRequest.getRequestURI());
            } else {
                log.warn("[CorrelationId] missing uuid header path={} method={}", httpRequest.getRequestURI(), httpRequest.getMethod());
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"code\":\"BAD_REQUEST\",\"message\":\"Missing required header: uuid\"}");
                return;
            }
        }

        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

        Map<String, String> headers = new LinkedHashMap<>();
        Collections.list(httpRequest.getHeaderNames())
                .forEach(name -> headers.put(name, httpRequest.getHeader(name)));
        log.debug("[CorrelationId] path={} method={} headers={}",
                httpRequest.getRequestURI(), httpRequest.getMethod(), headers);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }

    }

}
