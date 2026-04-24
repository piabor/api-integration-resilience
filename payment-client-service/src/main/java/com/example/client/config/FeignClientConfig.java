package com.example.client.config;

import com.example.client.exception.ProviderBadRequestException;
import com.example.client.exception.ProviderServerException;
import feign.Logger;
import feign.Response;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ProviderErrorDecoder();
    }

    static class ProviderErrorDecoder implements ErrorDecoder {

        @Override
        public Exception decode(String methodKey, Response response) {
            if (response.status() == 400) {
                return new ProviderBadRequestException("Provider returned 400 Bad Request");
            }

            if (response.status() >= 500) {
                return new ProviderServerException("Provider returned " + response.status());
            }

            return new RetryableException(
                    response.status(),
                    "Temporary provider error",
                    response.request().httpMethod(),
                    (Long) null,
                    response.request());
        }
    }
}
