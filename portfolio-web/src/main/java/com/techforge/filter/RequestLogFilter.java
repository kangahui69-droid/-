package com.techforge.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 请求日志过滤器 - 记录所有 API 请求
 * - 请求方法、URL
 * - 响应状态、耗时
 * - 异常堆栈
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLogFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger("REQUEST_LOG");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            // 异常时记录堆栈
            log.error("[{}] {} {} - ERROR - {}",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    getQueryString(httpRequest),
                    e.getMessage());
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 根据状态码选择日志级别
            if (httpResponse.getStatus() >= 500) {
                log.warn("[{}] {} {} - {}ms - {}",
                        httpRequest.getMethod(),
                        httpRequest.getRequestURI(),
                        getQueryString(httpRequest),
                        duration,
                        httpResponse.getStatus());
            } else if (httpResponse.getStatus() >= 400) {
                log.warn("[{}] {} {} - {}ms - {}",
                        httpRequest.getMethod(),
                        httpRequest.getRequestURI(),
                        getQueryString(httpRequest),
                        duration,
                        httpResponse.getStatus());
            } else {
                log.info("[{}] {} {} - {}ms - {}",
                        httpRequest.getMethod(),
                        httpRequest.getRequestURI(),
                        getQueryString(httpRequest),
                        duration,
                        httpResponse.getStatus());
            }
        }
    }

    private String getQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return queryString != null ? "?" + queryString : "";
    }
}