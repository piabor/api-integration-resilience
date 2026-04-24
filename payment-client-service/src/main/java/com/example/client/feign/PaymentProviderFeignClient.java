package com.example.client.feign;

import com.example.client.config.FeignClientConfig;
import com.example.client.dto.ProviderPaymentRequest;
import com.example.client.dto.ProviderPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "payment-provider",
        url = "${provider.base-url}",
        configuration = FeignClientConfig.class
)
public interface PaymentProviderFeignClient {

    @PostMapping("/provider/payments")
    ProviderPaymentResponse createPayment(
            @RequestHeader(value = "X-Mode", required = false) String mode,
            @RequestBody ProviderPaymentRequest request
    );
}
