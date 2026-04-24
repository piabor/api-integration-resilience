package com.example.provider.controller;

import com.example.provider.dto.ProviderPaymentRequest;
import com.example.provider.dto.ProviderPaymentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/provider/payments")
public class ProviderPaymentController {

    @PostMapping
    public ResponseEntity<ProviderPaymentResponse> createPayment(
            @Valid @RequestBody ProviderPaymentRequest request,
            @RequestHeader(value = "X-Mode", required = false) String modeHeader) throws InterruptedException {

        String effectiveMode = resolveMode(modeHeader);

        return switch (effectiveMode) {
            case "success" -> ResponseEntity.ok(buildSuccessResponse());
            case "slow" -> {
                Thread.sleep(5_000);
                yield ResponseEntity.ok(buildSuccessResponse());
            }
            case "fail" -> throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Provider failed");
            case "bad-request" -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provider rejected request");
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported X-Mode value");
        };
    }

    private String resolveMode(String modeHeader) {
        if (modeHeader != null && !modeHeader.isBlank()) {
            return modeHeader;
        }

        String[] randomModes = {"success", "slow", "fail"};
        int randomIndex = ThreadLocalRandom.current().nextInt(randomModes.length);
        return randomModes[randomIndex];
    }

    private ProviderPaymentResponse buildSuccessResponse() {
        return new ProviderPaymentResponse(
                UUID.randomUUID().toString(),
                "SUCCESS",
                "Payment processed by provider"
        );
    }
}
