package org.shopwave.cartservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Get current request attributes
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes)
                        RequestContextHolder
                                .getRequestAttributes();

                if (attributes != null) {
                    HttpServletRequest request =
                            attributes.getRequest();

                    // Get JWT token from incoming request
                    String authHeader =
                            request.getHeader("Authorization");

                    if (authHeader != null &&
                            authHeader.startsWith("Bearer ")) {
                        // Forward token to outgoing Feign call
                        template.header(
                                "Authorization", authHeader);
                        log.debug(
                                "Forwarding JWT token to: {}",
                                template.url());
                    } else {
                        log.warn(
                                "No Authorization header found " +
                                "for Feign call to: {}",
                                template.url());
                    }
                }
            }
        };
    }
}