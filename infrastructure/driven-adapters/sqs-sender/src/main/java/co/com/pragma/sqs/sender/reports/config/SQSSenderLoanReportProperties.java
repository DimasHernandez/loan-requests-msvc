package co.com.pragma.sqs.sender.reports.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqsapprovedloanreports")
public record SQSSenderLoanReportProperties(
        String region,
        String queueUrl,
        String endpoint
) {
}
