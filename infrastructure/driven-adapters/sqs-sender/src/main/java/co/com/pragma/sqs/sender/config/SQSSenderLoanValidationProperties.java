package co.com.pragma.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqsdebtcalculate")
public record SQSSenderLoanValidationProperties(
        String region,
        String queueUrl,
        String endpoint
) {
}
